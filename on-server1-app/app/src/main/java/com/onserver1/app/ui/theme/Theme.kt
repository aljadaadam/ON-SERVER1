package com.onserver1.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    secondary = SecondaryYellow,
    onSecondary = Color.Black,
    tertiary = SecondaryYellowDark,
    background = BackgroundLight,
    onBackground = OnPrimaryLight,
    surface = BackgroundLight,
    onSurface = OnPrimaryLight,
    surfaceVariant = CardLight,
    error = ErrorRed,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    secondary = SecondaryYellow,
    onSecondary = Color.Black,
    tertiary = SecondaryYellowDark,
    background = BackgroundDark,
    onBackground = OnPrimaryDark,
    surface = BackgroundDark,
    onSurface = OnPrimaryDark,
    surfaceVariant = CardDark,
    error = ErrorRed,
)

@Composable
fun OnServer1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val dimens = rememberDimens()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowInsetsControllerCompat(window, view)
            // Light icons on dark background, dark icons on light background
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalDimens provides dimens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
