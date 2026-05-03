package com.example.fleetsync

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "fleet_sync_prefs"
    private const val KEY_LANG = "selected_language"
    private const val KEY_VOICE_LANG = "voice_language"  // Separate key for TTS voice

    // ─── UI Language ──────────────────────────────────────────────────────────
    fun setLanguage(context: Context, langCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, langCode)
            .commit()   // synchronous flush before Activity.recreate()
    }

    fun getLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANG, "en") ?: "en"
    }

    // ─── Voice / TTS Language (independent of UI language) ────────────────────
    fun setVoiceLanguage(context: Context, langCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_VOICE_LANG, langCode)
            .apply()
    }

    fun getVoiceLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_VOICE_LANG, "en") ?: "en"
    }

    // ─── Locale helpers ────────────────────────────────────────────────────────
    fun codeToLocale(langCode: String): Locale = when (langCode) {
        "hi" -> Locale("hi", "IN")
        "mr" -> Locale("mr", "IN")
        "gu" -> Locale("gu", "IN")
        "ta" -> Locale("ta", "IN")
        "te" -> Locale("te", "IN")
        else -> Locale.ENGLISH
    }

    fun wrapContext(context: Context): Context {
        val locale = codeToLocale(getLanguage(context))
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
