package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class AppExtraColors(
    val lavender: Color,
    val lavenderContainer: Color,
    val coralContainer: Color,
    val mintContainer: Color,
    val sunbeamContainer: Color,
    val starGold: Color,
    val heartRose: Color,
    val correctGreen: Color,
    val wrongRed: Color
)

private val LightExtraColors = AppExtraColors(
    lavender = LavenderAccent,
    lavenderContainer = LavenderContainer,
    coralContainer = CoralSoftContainer,
    mintContainer = MintSoftContainer,
    sunbeamContainer = SunbeamSoftContainer,
    starGold = StarGoldColor,
    heartRose = HeartRoseColor,
    correctGreen = CorrectGreenColor,
    wrongRed = WrongRedColor
)

private val DarkExtraColors = AppExtraColors(
    lavender = Color(0xFFB39DFF),
    lavenderContainer = DarkLavenderContainer,
    coralContainer = DarkCoralContainer,
    mintContainer = DarkMintContainer,
    sunbeamContainer = DarkSunbeamContainer,
    starGold = Color(0xFFFFD166),
    heartRose = Color(0xFFFF6B8B),
    correctGreen = Color(0xFF4CE0A8),
    wrongRed = Color(0xFFFF7582)
)

val LocalAppExtraColors = staticCompositionLocalOf { LightExtraColors }

private val LightColorScheme = lightColorScheme(
    primary = BerryPrimary,
    onPrimary = BerryOnPrimary,
    primaryContainer = BerryPrimaryContainer,
    onPrimaryContainer = BerryOnPrimaryContainer,
    secondary = MintSecondary,
    onSecondary = MintOnSecondary,
    secondaryContainer = MintSecondaryContainer,
    onSecondaryContainer = MintOnSecondaryContainer,
    tertiary = GoldTertiary,
    onTertiary = GoldOnTertiary,
    tertiaryContainer = GoldTertiaryContainer,
    onTertiaryContainer = GoldOnTertiaryContainer,
    background = WarmParchmentBg,
    onBackground = DeepPlumInk,
    surface = WarmSurface,
    onSurface = DeepPlumInk,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = MutedPlumInk,
    outline = OutlineSoft,
    outlineVariant = OutlineSoft
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkBerryPrimary,
    onPrimary = DarkBerryOnPrimary,
    primaryContainer = DarkBerryPrimaryContainer,
    onPrimaryContainer = DarkBerryOnPrimaryContainer,
    secondary = DarkMintSecondary,
    onSecondary = DarkMintOnSecondary,
    secondaryContainer = DarkMintSecondaryContainer,
    onSecondaryContainer = DarkMintOnSecondaryContainer,
    tertiary = DarkGoldTertiary,
    onTertiary = DarkGoldOnTertiary,
    tertiaryContainer = DarkGoldTertiaryContainer,
    onTertiaryContainer = DarkGoldOnTertiaryContainer,
    background = DarkBerryBg,
    onBackground = DarkInkText,
    surface = DarkBerrySurface,
    onSurface = DarkInkText,
    surfaceVariant = DarkBerrySurfaceVariant,
    onSurfaceVariant = DarkMutedText,
    outline = DarkOutlineSoft,
    outlineVariant = DarkOutlineSoft
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun NajmatAlHalaqatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(LocalAppExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}

object AppTheme {
    val extraColors: AppExtraColors
        @Composable
        get() = LocalAppExtraColors.current
}
