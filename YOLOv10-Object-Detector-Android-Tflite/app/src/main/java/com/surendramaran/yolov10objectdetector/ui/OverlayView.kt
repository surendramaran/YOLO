package com.surendramaran.yolov10objectdetector.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.surendramaran.yolov10objectdetector.BoundingBox
import com.surendramaran.yolov10objectdetector.R

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results = listOf<BoundingBox>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var bounds = Rect()

    init {
        initPaints()
    }

    fun clear() {
        results = listOf()
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.WHITE
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 36f

        textPaint.color = Color.BLACK
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 36f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.white)
        boxPaint.strokeWidth = 6F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        results.forEach {

            val left = it.x1 * width
            val top = it.y1 * height
            val right = it.x2 * width
            val bottom = it.y2 * height

            canvas.drawRoundRect(left, top, right, bottom, 16f, 16f, boxPaint)
            val drawableText = "${it.clsName} ${Math.round(it.cnf * 100.0) / 100.0}"

            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()

            val textBackgroundRect = RectF(
                left,
                top,
                left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + BOUNDING_RECT_TEXT_PADDING
            )
            canvas.drawRoundRect(textBackgroundRect, 8f, 8f, textBackgroundPaint)

            canvas.drawText(drawableText, left, top + textHeight, textPaint)

        }
    }

    fun setResults(boundingBoxes: List<BoundingBox>) {
        results = boundingBoxes
        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}