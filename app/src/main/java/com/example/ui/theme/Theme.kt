package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = NavyDark,
    secondary = MediTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = MediTealLight,
    onSecondaryContainer = MediTealDark,
    tertiary = MediGreenSuccess,
    onTertiary = Color.White,
    tertiaryContainer = MediGreenLight,
    onTertiaryContainer = Color(0xFF064E3B),
    background = SoftBgPale,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = SoftBgPale,
    onSurfaceVariant = Slate700,
    outline = Slate300,
    error = MediRedUrgent,
    errorContainer = MediRedLight,
    onError = Color.White
)

private val AccessibleLightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = Color.Black,
    secondary = MediTealDark,
    onSecondary = Color.White,
    secondaryContainer = MediTealLight,
    onSecondaryContainer = Color.Black,
    tertiary = MediGreenSuccess,
    onTertiary = Color.White,
    tertiaryContainer = MediGreenLight,
    onTertiaryContainer = Color.Black,
    background = Color(0xFFE2E8F0),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFCBD5E1),
    onSurfaceVariant = Color.Black,
    outline = Color.Black,
    error = MediRedUrgent,
    errorContainer = MediRedLight,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF022C22),
    background = Color(0xFF0B1320),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF131D2E),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF881337),
    onError = Color(0xFF450A0A)
)

@Composable
fun MediKioskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAccessibilityMode: Boolean = false,
    content: @Composable () -> Unit
) {
    // For hospital waiting room kiosk, a clean, high-contrast light theme is best for accessibility
    val colorScheme = when {
        isAccessibilityMode -> AccessibleLightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val typography = if (isAccessibilityMode) AccessibleTypography else Typography

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
