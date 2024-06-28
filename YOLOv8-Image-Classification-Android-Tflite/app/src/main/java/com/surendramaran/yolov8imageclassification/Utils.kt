package com.surendramaran.yolov8imageclassification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileNotFoundException

object Utils {

    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }


    fun fileToBitmap(filePath: String): Bitmap? {
        return BitmapFactory.decodeFile(filePath)
    }

    fun createImageFile(context: Context): File {
        val uupDir = File(context.filesDir, "surendramaran.com")
        if (!uupDir.exists()) {
            uupDir.mkdir()
        }
        return File.createTempFile("${System.currentTimeMillis()}", ".jpg", uupDir)
    }
}