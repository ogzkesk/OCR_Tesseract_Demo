package com.ogzkesk.emptyproject

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import com.googlecode.tesseract.android.TessBaseAPI
import com.ogzkesk.emptyproject.ocr.MediaHelper
import com.ogzkesk.emptyproject.ocr.Ocr
import com.ogzkesk.emptyproject.ui.theme.EmptyProjectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), TessBaseAPI.ProgressNotifier {

    override fun onProgressValues(progressValues: TessBaseAPI.ProgressValues?) {
        progressState.value = progressValues != null
        println(progressValues)
    }

    private val progressState = mutableStateOf(false)
    private lateinit var mediaHelper: MediaHelper
    private lateinit var ocr: Ocr


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaHelper = MediaHelper(this)
        initClassifier()

        setContent {
            EmptyProjectTheme {
                Main(
                    isProgress = progressState.value,
                    mediaHelper = mediaHelper,
                    classifier = ocr
                )
            }
        }
    }

    private fun initClassifier() {
        TessBaseAPI.OEM_LSTM_ONLY
        val engineMode = Ocr.EngineMode.OEM_LSTM_ONLY
        val lang = Ocr.Language.EN
        ocr = Ocr.Builder(this)
                .setProgressNotifier(this)
                .setEngineMode(engineMode)
                .setLanguage(lang)
                .build()
    }

    override fun onDestroy() {
        mediaHelper.release()
        super.onDestroy()
    }
}

@Composable
fun Main(
    mediaHelper: MediaHelper,
    classifier: Ocr,
    isProgress: Boolean,
) {

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var imageBitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }
    var classifiedText by remember {
        mutableStateOf("")
    }

    LaunchedEffect(classifiedText){
        if(classifiedText.isNotEmpty()){
            scrollState.animateScrollTo(Int.MAX_VALUE)
        }
    }

    LaunchedEffect(Unit) {
        mediaHelper.setActivityResultListener {
            val uri = it.data ?: return@setActivityResultListener
            imageBitmap = mediaHelper.uriToBitmap(uri)?.asImageBitmap()
        }
    }

    ProgressDialog(state = isProgress)

    Scaffold { padd ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padd),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                FilledTonalIconButton(onClick = { mediaHelper.startGallery() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_gallery),
                        contentDescription = null
                    )
                }
                FilledTonalIconButton(onClick = { mediaHelper.startCamera() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = null
                    )
                }
            }

            if (imageBitmap != null) {
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    bitmap = imageBitmap!!,
                    contentDescription = null
                )
            }

            Button(
                content = { Text(text = "OCR") },
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        classifiedText = ""
                        if (imageBitmap != null) {
                            val bmp =
                                imageBitmap!!.asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)

                            classifier.recognizeImage(bmp) {
                                classifiedText = it ?: ""
                            }

                        } else {
                            context.showLongToast("No image found")
                        }
                    }
                }
            )

            if (classifiedText.isNotEmpty()) {
                Text(text = classifiedText)
            }
        }
    }
}


@Composable
fun ProgressDialog(state: Boolean) {
    if (state) {
        Dialog(onDismissRequest = { }) {
            CircularProgressIndicator()
        }
    }
}

