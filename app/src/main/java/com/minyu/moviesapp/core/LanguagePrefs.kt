package com.minyu.moviesapp.core

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import java.util.Locale


object LanguagePrefs {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    fun get(context: Context): String {
        val prefs: SharedPreferences =
            context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Retrieve the saved language code from SharePreferences
        return prefs.getString(KEY_LANGUAGE, "") ?: ""
        // Return the language code, or an empty string if not set
    }

    fun set(context: Context, language: String) {
        val normalized = Locale.forLanguageTag(language).toLanguageTag() // e.g., zh-hk -> zh-HK
        // Normalize the language code, or an empty string if not set
        val prefs: SharedPreferences =
            context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_LANGUAGE, normalized) }
    }

    fun setAndApply(context: Context, language: String) {
        set(context, language) // Save the selected language code to SharePreferences
        applyAppLocale(context)
    }

    // Apply the saved langauge as the app's locale
    fun applyAppLocale(context: Context) {
        val tag = get(context).ifBlank { Locale.getDefault().toLanguageTag() }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}