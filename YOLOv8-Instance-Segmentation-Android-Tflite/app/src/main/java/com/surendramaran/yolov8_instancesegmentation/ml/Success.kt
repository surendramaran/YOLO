package com.surendramaran.yolov8_instancesegmentation.ml

data class Success(
    val preProcessTime: Long,
    val interfaceTime: Long,
    val postProcessTime: Long,
    val results: List<SegmentationResult>
)