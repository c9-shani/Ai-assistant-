package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class AgentVoiceSpeaker(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady: Boolean = false
    var isEnabled: Boolean = true

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isReady = true
                tts?.setPitch(1.05f)
                tts?.setSpeechRate(1.1f)
            }
        }
    }

    fun speak(text: String) {
        if (!isEnabled || !isReady) return
        // Strip out code symbols or long markdown for natural speech
        val cleaned = text
            .replace(Regex("```[\\s\\S]*?```"), "Code block omitted.")
            .replace(Regex("[#*`~_]"), "")
            .take(300)
        tts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, "C9_SPEECH")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
