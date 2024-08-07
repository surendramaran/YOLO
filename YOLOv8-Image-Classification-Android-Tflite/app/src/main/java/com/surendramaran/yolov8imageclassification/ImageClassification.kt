package com.surendramaran.yolov8imageclassification

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.surendramaran.yolov8imageclassification.MetaData.extractNamesFromLabelFile
import com.surendramaran.yolov8imageclassification.MetaData.extractNamesFromMetadata
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class ImageClassification(
    private val context: Context,
    private val modelPath: String,
    private val labelPath: String?,
    private val classificationListener: ClassificationListener,
    private val message: (String) -> Unit
) {

    private var interpreter: Interpreter
    private var labels = mutableListOf<String>()

    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numClass = 0

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()

    init {

        /* Using GPU cause problems with Recycler View

        val compatList = CompatibilityList()
            Interpreter.Options().apply{
                if(compatList.isDelegateSupportedOnThisDevice){
                    val delegateOptions = compatList.bestOptionsForThisDevice
                    this.addDelegate(GpuDelegate(delegateOptions))
                } else {
                    this.setNumThreads(4)
                }
            }
        */

        val options = Interpreter.Options().apply{
           this.setNumThreads(4)
        }

        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = Interpreter(model, options)

        labels.addAll(extractNamesFromMetadata(model))
        if (labels.isEmpty()) {
            if (labelPath == null) {
                message("Model not contains metadata, provide LABELS_PATH in Constants.kt")
                labels.addAll(MetaData.TEMP_CLASSES)
            } else {
                labels.addAll(extractNamesFromLabelFile(context, labelPath))
            }
        }

        val inputShape = interpreter.getInputTensor(0)?.shape()
        val outputShape = interpreter.getOutputTensor(0)?.shape()

        if (inputShape != null) {
            tensorWidth = inputShape[1]
            tensorHeight = inputShape[2]

            // If in case input shape is in format of [1, 3, ..., ...]
            if (inputShape[1] == 3) {
                tensorWidth = inputShape[2]
                tensorHeight = inputShape[3]
            }
        }
        if (outputShape != null) {
            numClass = outputShape[1]
        }
    }

    fun close() {
        interpreter.close()
    }

    fun invoke(frame: Bitmap) {
        if (tensorWidth == 0) return
        if (tensorHeight == 0) return

        var inferenceTime = SystemClock.uptimeMillis()

        val resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false)

        val tensorImage = TensorImage(INPUT_IMAGE_TYPE)
        tensorImage.load(resizedBitmap)
        val processedImage = imageProcessor.process(tensorImage)
        val imageBuffer = processedImage.buffer

        val output = TensorBuffer.createFixedSize(intArrayOf(1 , numClass) , OUTPUT_IMAGE_TYPE)
        interpreter.run(imageBuffer, output.buffer)

        val outputArray = output.floatArray

        val predictions = mutableListOf<Prediction>()

        outputArray.forEachIndexed { index, float ->
            if (float > CONFIDENCE_THRESHOLD) {
                predictions.add(
                    Prediction(
                        id = index,
                        name = labels[index],
                        score = float
                    )
                )
            }
        }

        predictions.sortByDescending { it.score }

        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
        classificationListener.onResult(predictions, inferenceTime)
    }

    interface ClassificationListener {
        fun onResult(data: List<Prediction>, inferenceTime: Long)
    }


    companion object {
        private const val INPUT_MEAN = 0f
        private const val INPUT_STANDARD_DEVIATION = 255f
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
        private const val CONFIDENCE_THRESHOLD = 0.01F
    }
}