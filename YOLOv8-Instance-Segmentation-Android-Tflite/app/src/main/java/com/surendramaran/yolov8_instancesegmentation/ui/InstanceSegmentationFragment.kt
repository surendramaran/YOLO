package com.surendramaran.yolov8_instancesegmentation.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.surendramaran.yolov8_instancesegmentation.BuildConfig
import com.surendramaran.yolov8_instancesegmentation.Constants.LABELS_PATH
import com.surendramaran.yolov8_instancesegmentation.Constants.MODEL_PATH
import com.surendramaran.yolov8_instancesegmentation.R
import com.surendramaran.yolov8_instancesegmentation.databinding.DialogSettingsBinding
import com.surendramaran.yolov8_instancesegmentation.databinding.FragmentInstanceSegmentationBinding
import com.surendramaran.yolov8_instancesegmentation.ml.DrawImages
import com.surendramaran.yolov8_instancesegmentation.ml.InstanceSegmentation
import com.surendramaran.yolov8_instancesegmentation.ml.Success
import com.surendramaran.yolov8_instancesegmentation.utils.OrientationLiveData
import com.surendramaran.yolov8_instancesegmentation.utils.Utils
import com.surendramaran.yolov8_instancesegmentation.utils.Utils.addCarouselEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InstanceSegmentationFragment : Fragment(){
    private var _binding: FragmentInstanceSegmentationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by activityViewModels()

    private var instanceSegmentation: InstanceSegmentation? = null
    private lateinit var orientationLiveData: OrientationLiveData

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var drawImages: DrawImages

    private val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let {
            val rotated = Utils.getBitmapFromUri(requireContext(), it) ?: return@let
            runInstanceSegmentation(rotated)
        }
    }

    private var currentPhotoUri: Uri? = null
    private val photoCapture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            currentPhotoUri?.let { uri ->
                val bitmap = Utils.getBitmapFromUri(requireContext(), uri) ?: return@let
                val rotated = Utils.rotateImageIfRequired(requireContext(), bitmap, uri)
                runInstanceSegmentation(rotated)
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
        _binding = FragmentInstanceSegmentationBinding.inflate(inflater, container, false)
        viewPagerAdapter = ViewPagerAdapter(mutableListOf())
        binding.viewpager.adapter = viewPagerAdapter
        binding.viewpager.addCarouselEffect()
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        instanceSegmentation = InstanceSegmentation(
            context = requireContext(),
            modelPath = MODEL_PATH,
            labelPath = LABELS_PATH
        ) {
            toast(it)
        }


        drawImages = DrawImages(requireContext())

        orientationLiveData = OrientationLiveData(requireContext(), characteristics).apply {
            observe(viewLifecycleOwner) { orientation ->
                Log.d(InstanceSegmentationFragment::class.java.simpleName, "Orientation changed: $orientation")
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

            ivSettings.setOnClickListener {
                showSettingsDialog()
            }
        }
    }

    private fun runInstanceSegmentation(bitmap: Bitmap) {
        lifecycleScope.launch(Dispatchers.Default) {
            instanceSegmentation?.invoke(
                frame = bitmap,
                smoothEdges = viewModel.isSmoothEdges,
                onSuccess = {  processSuccessResult(bitmap, it) },
                onFailure = { clearOutput(it) }
            )
        }
    }

    private fun processSuccessResult(original: Bitmap, success: Success) {
        requireActivity().runOnUiThread {
            binding.apply {
                tvPreProcess.text = getString(R.string.interface_time_value, success.preProcessTime.toString())
                tvInterfaceTime.text = getString(R.string.interface_time_value, success.interfaceTime.toString())
                tvPostProcess.text = getString(R.string.interface_time_value, success.postProcessTime.toString())
            }
        }

        val images = drawImages.invoke(
            original = original,
            success = success,
            isSeparateOut = viewModel.isSeparateOutChecked,
            isMaskOut = viewModel.isMaskOutChecked
        )

        requireActivity().runOnUiThread {
            viewPagerAdapter.updateImages(images)
        }

    }

    private fun clearOutput(error: String) {
        requireActivity().runOnUiThread {
            binding.apply {
                tvPreProcess.text = getString(R.string.empty_string)
                tvInterfaceTime.text = getString(R.string.empty_string)
                tvPostProcess.text = getString(R.string.empty_string)
            }
            viewPagerAdapter.updateImages(mutableListOf())
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instanceSegmentation?.close()
    }

    private fun showSettingsDialog() {

        val dialog = Dialog(requireContext())
        val customDialogBoxBinding = DialogSettingsBinding.inflate(layoutInflater)
        dialog.setContentView(customDialogBoxBinding.root)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        customDialogBoxBinding.apply {

            cbSeparateOut.isChecked = viewModel.isSeparateOutChecked
            cbMaskOut.isChecked = viewModel.isMaskOutChecked
            cbSmoothEdges.isChecked = viewModel.isSmoothEdges

            cbSeparateOut.setOnCheckedChangeListener { _, isChecked ->
                viewModel.isSeparateOutChecked = isChecked
            }

            cbMaskOut.setOnCheckedChangeListener { _, isChecked ->
                viewModel.isMaskOutChecked = isChecked
            }

            cbSmoothEdges.setOnCheckedChangeListener { _, isChecked ->
                viewModel.isSmoothEdges = isChecked
            }

            llPatreon.setOnClickListener {
                val webpage: Uri = Uri.parse("https://www.patreon.com/SurendraMaran")
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                startActivity(intent)
            }
        }
        dialog.show()
    }

    private fun toast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }
}