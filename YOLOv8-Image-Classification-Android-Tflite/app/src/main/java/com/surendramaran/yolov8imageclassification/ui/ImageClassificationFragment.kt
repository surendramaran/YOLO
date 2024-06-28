package com.surendramaran.yolov8imageclassification.ui

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.surendramaran.yolov8imageclassification.BuildConfig
import com.surendramaran.yolov8imageclassification.Constants
import com.surendramaran.yolov8imageclassification.Constants.LABELS_PATH
import com.surendramaran.yolov8imageclassification.Constants.MODEL_PATH
import com.surendramaran.yolov8imageclassification.ImageClassification
import com.surendramaran.yolov8imageclassification.PredicationAdapter
import com.surendramaran.yolov8imageclassification.Prediction
import com.surendramaran.yolov8imageclassification.R
import com.surendramaran.yolov8imageclassification.Utils
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

    private lateinit var backgroundExecutor: ExecutorService

    private val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let {
            val bitmap = Utils.uriToBitmap(requireContext(), it) ?: return@let
            runClassification(bitmap)
        }
    }

    private var currentPhotoFile: File? = null
    private val photoCapture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            currentPhotoFile?.let { file ->
                val bitmap = Utils.fileToBitmap(file.absolutePath) ?: return@let
                runClassification(bitmap)
            }
        }
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
            imageClassification = ImageClassification(requireContext(), MODEL_PATH, LABELS_PATH, this)
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
                currentPhotoFile = photoFile
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

    override fun onDestroy() {
        super.onDestroy()
        imageClassification?.close()
        backgroundExecutor.shutdown()
    }

    override fun onResult(data: List<Prediction>, inferenceTime: Long) {
        lifecycleScope.launch(Dispatchers.Main) {
            predicationAdapter.submitList(data)
            binding.llInterfaceTime.visibility = View.VISIBLE
            binding.tvInterfaceTime.text = getString(R.string.interface_time_value, inferenceTime.toString())
        }
    }

}