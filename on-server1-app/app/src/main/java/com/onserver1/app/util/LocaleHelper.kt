package com.onserver1.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Manages in-app language override independent of device locale.
 * Stores preference in SharedPreferences and wraps the Context.
 */
object LocaleHelper {

    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "app_language"
    const val LANGUAGE_AR = "ar"
    const val LANGUAGE_EN = "en"

    /**
     * Get saved language code. Returns null if user hasn't chosen yet (use device default).
     */
    fun getSavedLanguage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, null)
    }

    /**
     * Save the user's language preference.
     */
    fun saveLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
    }

    /**
     * Get the effective language — saved preference or device default.
     */
    fun getEffectiveLanguage(context: Context): String {
        return getSavedLanguage(context) ?: getDeviceLanguage()
    }

    /**
     * Get the device's primary language.
     */
    private fun getDeviceLanguage(): String {
        return Locale.getDefault().language
    }

    /**
     * Wrap the context with the configured locale.
     * Call this from attachBaseContext() in both Application and Activity.
     */
    fun wrap(context: Context): Context {
        val savedLang = getSavedLanguage(context) ?: return context
        return updateContextLocale(context, savedLang)
    }

    /**
     * Apply locale to a context, returning a new context with the locale set.
     */
    private fun updateContextLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}
