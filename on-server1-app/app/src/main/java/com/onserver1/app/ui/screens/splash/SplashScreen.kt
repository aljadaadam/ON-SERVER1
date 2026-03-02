package com.onserver1.app.ui.screens.splash

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Splash screen wrapper – delegates to Cinematic or Classic based on user preference.
 * Default: Cinematic (new box-opening animation).
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("splash_prefs", Context.MODE_PRIVATE)
    val useCinematic = prefs.getBoolean("cinematic_splash", true)

    if (useCinematic) {
        SplashScreenCinematic(onSplashFinished = onSplashFinished)
    } else {
        SplashScreenClassic(onSplashFinished = onSplashFinished)
    }
}
