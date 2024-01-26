package com.ogzkesk.emptyproject.ocr

import android.content.Context
import android.content.res.AssetManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object OcrUtils {


    fun getLocalDir(context: Context): File {
        return context.filesDir
    }

    fun getTessDataPath(context: Context): String {
        return getLocalDir(context).absolutePath
    }

    fun extractAssets(context: Context) {
        val am = context.assets
        val localDir: File = getLocalDir(context)
        if (!localDir.exists() && !localDir.mkdir()) {
            throw RuntimeException("Can't create directory $localDir")
        }
        val tessDir: File = File(getTessDataPath(context), "tessdata")
        if (!tessDir.exists() && !tessDir.mkdir()) {
            throw RuntimeException("Can't create directory $tessDir")
        }

        try {
            for (assetName in am.list("")!!) {
                val targetFile: File
                targetFile = if (assetName.endsWith(".traineddata")) {
                    File(tessDir, assetName)
                } else {
                    continue
//                File(localDir, assetName)
                }
                if (!targetFile.exists()) {
                    copyFile(am, assetName, targetFile)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun copyFile(
        am: AssetManager, assetName: String,
        outFile: File,
    ) {
        try {
            am.open(assetName).use { `in` ->
                FileOutputStream(outFile).use { out ->
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (`in`.read(buffer).also { read = it } != -1) {
                        out.write(buffer, 0, read)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}