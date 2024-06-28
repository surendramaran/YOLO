package com.surendramaran.yolov8imageclassification

data class Prediction(
    val id: Int,
    val name: String,
    val score: Float
)
