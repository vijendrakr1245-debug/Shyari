package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val KavyaDarkColorScheme = darkColorScheme(
    primary = RoyalPurplePrimary,
    onPrimary = Color(0xFF140726),
    primaryContainer = RoyalPurpleContainer,
    onPrimaryContainer = OnRoyalPurpleContainer,
    secondary = GoldenAmberTertiary,
    onSecondary = Color(0xFF261800),
    secondaryContainer = GoldenAmberContainer,
    onSecondaryContainer = OnGoldenAmberContainer,
    tertiary = GoldenAmberTertiary,
    onTertiary = Color(0xFF281900),
    tertiaryContainer = GoldenAmberContainer,
    onTertiaryContainer = OnGoldenAmberContainer,
    background = MidnightBackground,
    onBackground = TextPrimaryLight,
    surface = MidnightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = TextSecondaryMuted,
    outline = MidnightCardBorder,
    error = ErrorCoral,
    errorContainer = ErrorContainerDark,
    onError = Color(0xFF2D0609),
    onErrorContainer = Color(0xFFFECACA)
)

private val KavyaLightColorScheme = lightColorScheme(
    primary = LightPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = LightPurpleContainer,
    onPrimaryContainer = OnLightPurpleContainer,
    secondary = LightGoldTertiary,
    onSecondary = Color.White,
    secondaryContainer = LightGoldContainer,
    onSecondaryContainer = OnLightGoldContainer,
    tertiary = LightGoldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = LightGoldContainer,
    onTertiaryContainer = OnLightGoldContainer,
    background = LightBackground,
    onBackground = Color(0xFF1A102B),
    surface = LightSurface,
    onSurface = Color(0xFF1A102B),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A3B69),
    outline = Color(0xFFC4B5FD)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) KavyaDarkColorScheme else KavyaLightColorScheme,
        typography = createAppTypography(fontScale),
        shapes = AppShapes,
        content = content
    )
}
