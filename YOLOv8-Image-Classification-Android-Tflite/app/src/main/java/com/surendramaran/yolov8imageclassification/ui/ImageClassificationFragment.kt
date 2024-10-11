package com.surendramaran.yolov8imageclassification.ui

import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.surendramaran.yolov8imageclassification.BuildConfig
import com.surendramaran.yolov8imageclassification.Constants.LABELS_PATH
import com.surendramaran.yolov8imageclassification.Constants.MODEL_PATH
import com.surendramaran.yolov8imageclassification.ImageClassification
import com.surendramaran.yolov8imageclassification.utils.OrientationLiveData
import com.surendramaran.yolov8imageclassification.utils.PredicationAdapter
import com.surendramaran.yolov8imageclassification.Prediction
import com.surendramaran.yolov8imageclassification.R
import com.surendramaran.yolov8imageclassification.utils.Utils
import com.surendramaran.yolov8imageclassification.databinding.FragmentImageClassificationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ImageClassificationFragment : Fragment(), ImageClassification.ClassificationListener {
    private var _binding: FragmentImageClassificationBinding? = null
    private val binding get() = _binding!!

    private var imageClassification: ImageClassification? = null
    private lateinit var predicationAdapter: PredicationAdapter
    private lateinit var orientationLiveData: OrientationLiveData

    private lateinit var backgroundExecutor: ExecutorService

    private val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let {
            val rotated = Utils.getBitmapFromUri(requireContext(), it) ?: return@let
            runClassification(rotated)
        }
    }


    private var currentPhotoUri: Uri? = null
    private val photoCapture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            currentPhotoUri?.let { uri ->
                val bitmap = Utils.getBitmapFromUri(requireContext(), uri) ?: return@let
                val rotated = Utils.rotateImageIfRequired(requireContext(), bitmap, uri)
                runClassification(rotated)
            }
        }
    }

    private val cameraManager: CameraManager by lazy {
        val context = requireContext().applicationContext
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val characteristics: CameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(Utils.getCameraId(cameraManager))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImageClassificationBinding.inflate(inflater, container, false)
        predicationAdapter = PredicationAdapter()
        binding.rvPredication.adapter = predicationAdapter
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backgroundExecutor = Executors.newSingleThreadExecutor()

        backgroundExecutor.execute {
            imageClassification = ImageClassification(requireContext(), MODEL_PATH, LABELS_PATH, this) {
                toast(it)
            }
        }

        orientationLiveData = OrientationLiveData(requireContext(), characteristics).apply {
            observe(viewLifecycleOwner) { orientation ->
                Log.d(ImageClassificationFragment::class.java.simpleName, "Orientation changed: $orientation")
            }
        }

        bindListeners()
    }


    private fun bindListeners() {
        binding.apply {
            btnCamera.setOnClickListener {
                val photoFile = Utils.createImageFile(requireContext())
                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${BuildConfig.APPLICATION_ID}.provider",
                    photoFile
                )
                currentPhotoUri = photoUri
                photoCapture.launch(photoUri)
            }

            btnGallery.setOnClickListener {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    private fun runClassification(bitmap: Bitmap) {
        binding.ivMain.setImageBitmap(bitmap)
        backgroundExecutor.submit {
            imageClassification?.invoke(bitmap)
        }
    }

    private fun toast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageClassification?.close()
        backgroundExecutor.shutdown()
    }

    override fun onResult(data: List<Prediction>, inferenceTime: Long) {
        lifecycleScope.launch(Dispatchers.Main) {
            predicationAdapter.setData(data)
            binding.llInterfaceTime.visibility = View.VISIBLE
            binding.tvInterfaceTime.text = getString(R.string.interface_time_value, inferenceTime.toString())
        }
    }

}