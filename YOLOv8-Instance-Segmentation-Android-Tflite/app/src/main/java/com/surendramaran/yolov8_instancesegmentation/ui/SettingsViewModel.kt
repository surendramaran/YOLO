package com.surendramaran.yolov8_instancesegmentation.ui

import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    var isSeparateOutChecked = true
    var isSmoothEdges = false
    var isMaskOutChecked = false
}