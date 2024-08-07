package com.surendramaran.yolov8_instancesegmentation

object Constants {
    const val MODEL_PATH = "yolov8n-seg_float16.tflite"
    val LABELS_PATH: String? = null

    // enable this to get smooth edges but result in more post process time
    const val SMOOTH_EDGES = true
}
