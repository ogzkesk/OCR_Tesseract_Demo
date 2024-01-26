package com.ogzkesk.emptyproject.ocr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.ogzkesk.emptyproject.showLongToast

class MediaHelper(private val context: Context) {

    private var activityListener: ((data: Intent) -> Unit)? = null
    private val activity: ComponentActivity = context as ComponentActivity

    private var activityLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    if (data == null) return@registerForActivityResult
                    Log.d(TAG, "intent data : ${data.data}")
                    activityListener?.invoke(data)
                }

                ImagePicker.RESULT_ERROR -> {
                    activity.showLongToast(ImagePicker.getError(data))
                }

                else -> {
                    activity.showLongToast("Task Cancelled")
                }
            }
        }


    fun startCamera() {
        ImagePicker.with(activity).provider(ImageProvider.CAMERA).createIntent { intent ->
            activityLauncher.launch(intent)
        }
    }

    fun startGallery() {
        ImagePicker.with(activity).provider(ImageProvider.GALLERY).createIntent { intent ->
            activityLauncher.launch(intent)
        }
    }

    fun setActivityResultListener(listener: (Intent) -> Unit) = apply {
        this.activityListener = listener
    }


    fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun release() {
        activityLauncher.unregister()
    }

    companion object {
        private const val TAG = "MediaHelper"
    }
}