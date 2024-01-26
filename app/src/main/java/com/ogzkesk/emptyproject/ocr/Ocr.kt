package com.ogzkesk.emptyproject.ocr

import android.content.Context
import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI
import com.googlecode.tesseract.android.TessBaseAPI.ProgressNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Ocr private constructor(
    language: String,
    engineMode: Int,
    context: Context,
    private var isInitialized: Boolean,
    private val tessBaseAPI: TessBaseAPI,
) {


    init {
        isInitialized = try {
            OcrUtils.extractAssets(context)
            val path = OcrUtils.getTessDataPath(context)
            tessBaseAPI.init(path, language, engineMode)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun recognizeImage(bitmap: Bitmap, onResult: (String?) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                if (!isInitialized) {
                    throw IllegalStateException("Ocr not initialized")
                }
                tessBaseAPI.setImage(bitmap)
                onResult.invoke(tessBaseAPI.utF8Text)
//                onResult.invoke(tessBaseAPI.getHOCRText(0)) to progress available, and stoppable
            } catch (e: Exception) {
                e.printStackTrace()
                onResult.invoke(null)
            } finally {
                clearPreviousImage()
            }
        }
    }


    fun stop() {
        tessBaseAPI.stop()
    }

    fun getAccuracy(): Int {
        return tessBaseAPI.meanConfidence()
    }

    fun tearDown() {
        tessBaseAPI.recycle()
    }

    fun clearPreviousImage() {
        tessBaseAPI.clear()
    }


    class Builder(private val context: Context) {

        private var progressNotifier: ProgressNotifier? = null
        private var engineMode: Int = EngineMode.OEM_DEFAULT.mode
        private var language: String = Language.EN.lang

        fun setProgressNotifier(progressNotifier: ProgressNotifier): Builder {
            this.progressNotifier = progressNotifier
            return this
        }

        fun setEngineMode(engineMode: EngineMode): Builder {
            this.engineMode = engineMode.mode
            return this
        }

        fun setLanguage(lang: Language): Builder {
            this.language = lang.lang
            return this
        }

        fun build(): Ocr {
            return Ocr(
                context = context,
                language = language,
                engineMode = engineMode,
                tessBaseAPI = TessBaseAPI(progressNotifier),
                isInitialized = false
            )
        }
    }


    enum class EngineMode(val mode: Int) {
        /**
         * Run Tesseract only - fastest
         */
        OEM_ONLY(0),
        /**
         * Run LSTM only - better accuracy, but slower
         */
        OEM_LSTM_ONLY(1),
        /**
         * Run both and combine results - best accuracy
         */
        OEM_LSTM_COMBINED(2),
        /**
         * Default OCR engine mode.
         */
        OEM_DEFAULT(3)
    }


    enum class Language(val lang: String) {
        // Language only english available for now with [eng.traineddata] in assets.
        EN("eng"),
        TR("tr")
    }
}