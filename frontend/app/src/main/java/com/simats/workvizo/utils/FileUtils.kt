package com.simats.workvizo.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    fun getFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}")

        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)

        outputStream.close()
        inputStream?.close()

        return file
    }
}
