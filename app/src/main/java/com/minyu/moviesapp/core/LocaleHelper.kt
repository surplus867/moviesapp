package com.minyu.moviesapp.core

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

// Helper object to update the app's context with a new locale
object LocaleHelper {
    // Wraps the given context with the specified Language/locals
    fun wrapWithLocale(context: Context, language: String): Context {
        val locale = Locale.forLanguageTag(language) // Create a Locale from the language tag
        Locale.setDefault(locale) // Set this locale as the default
        val config = Configuration(context.resources.configuration) // Copy current configuration
        config.setLocale(locale) // Set the new locale in the configuration
        return context.createConfigurationContext(config) // Return a context with the updated locale
    }
}