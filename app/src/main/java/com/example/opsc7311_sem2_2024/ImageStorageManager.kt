package com.example.opsc7311_sem2_2024

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

class ImageStorageManager {

    // Save the image to internal storage and return the file path
    fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, fileName: String): String {
        val directory = context.filesDir
        val file = File(directory, "$fileName.jpg")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.close()
        return file.absolutePath
    }

    // Retrieve the image from internal storage using the file path
    fun getImageFromInternalStorage(filePath: String): Bitmap? {
        val imgFile = File(filePath)
        return if (imgFile.exists()) {
            BitmapFactory.decodeFile(imgFile.absolutePath)
        } else {
            null
        }
    }

    // Optional: Delete image from internal storage
    fun deleteImageFromInternalStorage(filePath: String): Boolean {
        val file = File(filePath)
        return file.delete()
    }

}