package com.surendramaran.yolov8_instancesegmentation.ml

data class SegmentationResult(
    val box: Output0,
    val mask: List<DoubleArray>
)