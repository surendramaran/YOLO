package com.surendramaran.yolov8_instancesegmentation.ml

import kotlin.math.exp

object ImageUtils {
    fun List<IntArray>.scaleMask(targetWidth: Int, targetHeight: Int): List<IntArray> {
        val originalHeight = this.size
        val originalWidth = this[0].size

        val xRatio = originalWidth.toDouble() / targetWidth
        val yRatio = originalHeight.toDouble() / targetHeight

        val output = List(targetHeight) { IntArray(targetWidth) }

        for (y in 0 until targetHeight) {
            for (x in 0 until targetWidth) {
                val origX = (x * xRatio).toInt()
                val origY = (y * yRatio).toInt()
                output[y][x] = this[origY][origX]
            }
        }

        return output
    }

    fun Array<FloatArray>.toMask() : List<IntArray> {
        return this.map { floatArray ->
            floatArray.map { if (it > 0) 1 else 0 }.toIntArray()
        }
    }


    fun List<IntArray>.smooth(kernel: Int) : List<IntArray> {
        val maskFloat = this.map { intArray ->
            intArray.map { if (it > 0) 1F else 0F }.toFloatArray()
        }
        val gaussianKernel = createGaussianKernel(kernel)
        val blurredImage = applyGaussianBlur(maskFloat, gaussianKernel)
        return thresholdImage(blurredImage)
    }

    private fun createGaussianKernel(size: Int): Array<FloatArray> {
        val sigma = 2F
        val kernel = Array(size) { FloatArray(size) }
        val mean = size / 2
        var sum = 0F

        for (x in 0 until size) {
            for (y in 0 until size) {
                kernel[x][y] = (1F / (2F * Math.PI.toFloat() * sigma * sigma)) * exp(
                    -((x - mean) * (x - mean) + (y - mean) * (y - mean)) / (2F * sigma * sigma)
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

    private fun applyGaussianBlur(image: List<FloatArray>, kernel: Array<FloatArray>): List<FloatArray> {
        val height = image.size
        val width = image[0].size
        val kernelSize = kernel.size
        val offset = kernelSize / 2
        val blurredImage = List(height) { FloatArray(width) { 0F } }

        for (i in offset until height - offset) {
            for (j in offset until width - offset) {
                var sum = 0F
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

    private fun thresholdImage(image: List<FloatArray>): List<IntArray> {
        val height = image.size
        val width = image[0].size
        return List(height) { i ->
            IntArray(width) { j ->
                if (image[i][j] > 0.9F) 1 else 0
            }
        }
    }
}