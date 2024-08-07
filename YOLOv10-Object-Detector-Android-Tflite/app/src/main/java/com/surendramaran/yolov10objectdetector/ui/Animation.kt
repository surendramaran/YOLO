package com.surendramaran.yolov10objectdetector.ui

import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView

object Animation {

    fun animateArrow(imageView: ImageView, startAngle: Float, endAngle: Float) {
        val animator = ValueAnimator.ofFloat(startAngle, endAngle).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                imageView.rotation = value
            }
        }
        animator.start()
    }
}