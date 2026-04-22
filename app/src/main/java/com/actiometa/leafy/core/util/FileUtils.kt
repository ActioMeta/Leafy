package com.actiometa.leafy.core.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    fun createTempImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.cacheDir
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        val file = createTempImageFile(context)
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    fun saveImageToInternalStorage(context: Context, sourceFile: File): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PLANT_$timeStamp.jpg"
        val destinationFile = File(context.filesDir, fileName)
        
        sourceFile.copyTo(destinationFile, overwrite = true)
        return destinationFile.absolutePath
    }
}
