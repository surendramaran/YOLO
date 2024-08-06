package com.surendramaran.yolov8_instancesegmentation.utils

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.view.OrientationEventListener
import androidx.lifecycle.LiveData

class OrientationLiveData(context: Context, characteristics: CameraCharacteristics): LiveData<Int>() {

    private val listener = object : OrientationEventListener(context.applicationContext) {
        override fun onOrientationChanged(orientation: Int) {
            val rotation = when {
                orientation <= 45 -> 0
                orientation <= 135 -> 90
                orientation <= 225 -> 180
                orientation <= 315 -> 270
                else -> 0
            }
            val sensorOrientationDegrees =
                characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

            val relative = (sensorOrientationDegrees + rotation + 360) % 360
            if (relative != value) postValue(relative)
        }
    }

    override fun onActive() {
        super.onActive()
        listener.enable()
    }

    override fun onInactive() {
        super.onInactive()
        listener.disable()
    }
}
