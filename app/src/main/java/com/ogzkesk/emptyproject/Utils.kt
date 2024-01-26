package com.ogzkesk.emptyproject

import android.content.Context
import android.content.res.AssetManager
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

