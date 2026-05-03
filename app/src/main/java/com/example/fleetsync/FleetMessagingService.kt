package com.example.fleetsync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class FleetMessagingService : FirebaseMessagingService() {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false   // guard: only speak when engine is fully initialised

    // ─────────────────────────────────────────────────────────────────────────
    override fun onCreate() {
        super.onCreate()
        initTts()
    }

    // ─── TTS Initialisation (crash-safe) ─────────────────────────────────────
    private fun initTts() {
        try {
            tts = TextToSpeech(applicationContext) { status ->
                isTtsReady = (status == TextToSpeech.SUCCESS)
                if (isTtsReady) {
                    applyVoiceLanguage()
                } else {
                    Log.w("FleetTTS", "TTS init failed with status=$status — voice alerts disabled")
                }
            }
        } catch (e: Exception) {
            Log.e("FleetTTS", "TTS constructor threw: ${e.message}")
        }
    }

    /**
     * Reads the saved voice-language pref and applies it to the TTS engine.
     * Falls back to English if the locale is missing/unsupported.
     * Wraps setLanguage() in try/catch — this is the primary crash point.
     */
    private fun applyVoiceLanguage() {
        val ttsEngine = tts ?: return
        val langCode  = LanguageManager.getVoiceLanguage(applicationContext)
        val locale    = LanguageManager.codeToLocale(langCode)
        try {
            val result = ttsEngine.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("FleetTTS", "Locale $locale not supported; falling back to English")
                ttsEngine.setLanguage(Locale.ENGLISH)
            }
        } catch (e: Exception) {
            Log.e("FleetTTS", "setLanguage threw: ${e.message} — falling back to English")
            try { ttsEngine.setLanguage(Locale.ENGLISH) } catch (_: Exception) { isTtsReady = false }
        }
    }

    // ─── FCM Message Handling ─────────────────────────────────────────────────
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "New Alert"
        val body  = remoteMessage.notification?.body  ?: remoteMessage.data["body"]  ?: "Check FleetSync"
        showNotification(title, body)
        speakAlert(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "fleet_alerts_channel"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "FleetSync Alerts", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        nm.notify(
            System.currentTimeMillis().toInt(),
            NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build()
        )
    }

    private fun speakAlert(title: String, body: String) {
        if (!isTtsReady) return
        try {
            // Re-apply language each time so user can change voice lang without restart
            applyVoiceLanguage()
            val text = if (title.contains("Toll", ignoreCase = true)) body else "$title. $body"
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fleet_notif")
        } catch (e: Exception) {
            Log.e("FleetTTS", "speak() threw: ${e.message}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Optionally persist to Firestore for targeted FCM messages
    }

    // ─────────────────────────────────────────────────────────────────────────
    override fun onDestroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("FleetTTS", "onDestroy cleanup: ${e.message}")
        } finally {
            tts = null
            isTtsReady = false
        }
        super.onDestroy()
    }
}
