package com.surendramaran.yolov8_instancesegmentation.ml

import kotlin.math.exp

object ImageUtils {
    fun List<DoubleArray>.scaleMask(targetWidth: Int, targetHeight: Int): List<DoubleArray> {
        val originalHeight = this.size
        val originalWidth = this[0].size

        val xRatio = originalWidth.toDouble() / targetWidth
        val yRatio = originalHeight.toDouble() / targetHeight

        val output = List(targetHeight) { DoubleArray(targetWidth) }

        for (y in 0 until targetHeight) {
            for (x in 0 until targetWidth) {
                val origX = (x * xRatio).toInt()
                val origY = (y * yRatio).toInt()
                output[y][x] = this[origY][origX]
            }
        }

        return output
    }

    fun Array<FloatArray>.toMask() : List<DoubleArray> {
        return this.map { floatArray ->
            floatArray.map { if (it > 0) 255.0 else 0.0 }.toDoubleArray()
        }
    }


    fun List<DoubleArray>.smooth(kernel: Int) : List<DoubleArray> {
        val gaussianKernel = createGaussianKernel(kernel)
        val blurredImage = applyGaussianBlur(this, gaussianKernel)
        return thresholdImage(blurredImage)
    }

    private fun createGaussianKernel(size: Int): Array<DoubleArray> {
        val sigma = 1.0
        val kernel = Array(size) { DoubleArray(size) }
        val mean = size / 2
        var sum = 0.0

        for (x in 0 until size) {
            for (y in 0 until size) {
                kernel[x][y] = (1 / (2 * Math.PI * sigma * sigma)) * exp(
                    -((x - mean) * (x - mean) + (y - mean) * (y - mean)) / (2 * sigma * sigma)
                )
                sum += kernel[x][y]
            }
        }

        for (x in 0 until size) {
            for (y in 0 until size) {
                kernel[x][y] /= sum
            }
        }

        return kernel
    }

    private fun applyGaussianBlur(image: List<DoubleArray>, kernel: Array<DoubleArray>): List<DoubleArray> {
        val height = image.size
        val width = image[0].size
        val kernelSize = kernel.size
        val offset = kernelSize / 2
        val blurredImage = List(height) { DoubleArray(width) { 0.0 } }

        for (i in offset until height - offset) {
            for (j in offset until width - offset) {
                var sum = 0.0
                for (ki in 0 until kernelSize) {
                    for (kj in 0 until kernelSize) {
                        val pixel = image[i - offset + ki][j - offset + kj]
                        sum += pixel * kernel[ki][kj]
                    }
                }
                blurredImage[i][j] = sum
            }
        }

        return blurredImage
    }

    private fun thresholdImage(image: List<DoubleArray>): List<DoubleArray> {
        val height = image.size
        val width = image[0].size
        return List(height) { i ->
            DoubleArray(width) { j ->
                if (image[i][j] > 127.5) 255.0 else 0.0
            }
        }
    }
}