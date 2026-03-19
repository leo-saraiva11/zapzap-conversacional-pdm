package com.example.zapzap.ui.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ImageUtils {
    fun createTempImageUri(context: Context): Uri {
        val file = File.createTempFile("IMG_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    fun createTempVideoUri(context: Context): Uri {
        val file = File.createTempFile("VID_", ".mp4", context.cacheDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
}
