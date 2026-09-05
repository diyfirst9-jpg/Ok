package com.winlator.cmod.shared.theme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winlator.cmod.R

// Orange-black palette: this file is the single source of truth for the app's
// Compose color tokens. True/solid black surfaces (no warm brown tint) with one
// saturated orange accent; corner radius + border width carry depth instead of
// elevation/shadow, so the surface steps below only need to be a few percent
// apart to read as "layered" against a solid black background.
val WinLiteBackground = Color(0xFF000000)
val WinLiteSurface = Color(0xFF0D0D0D)
val WinLiteSurfaceAlt = Color(0xFF161616)
val WinLitePanel = Color(0xFF000000)
val WinLiteOutline = Color(0xFF262626)
val WinLiteAccent = Color(0xFFFF7A00) // primary orange
val WinLiteAccentAlt = Color(0xFFFFA940) // secondary accent for status/links
val WinLiteTextPrimary = Color(0xFFF5F0EA)
val WinLiteTextSecondary = Color(0xFFAD9782)
val WinLiteDanger = Color(0xFFFF7A88)

// Flat design tokens: no elevation/shadow, corner radius and border width
// are the only depth cues. Cheaper to draw than shadow() (no extra
// rasterization/blur pass) and matches the flat Switch home-menu look.
val WinLiteCardShape = RoundedCornerShape(16.dp)
val WinLiteChipShape = RoundedCornerShape(12.dp)
val WinLiteBorderWidth = 1.dp

private val WinLiteShapes =
    Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
    )

private val WinLiteColorScheme =
    darkColorScheme(
        primary = WinLiteAccent,
        background = WinLiteBackground,
        surface = WinLiteSurface,
        onSurface = WinLiteTextPrimary,
        onBackground = WinLiteTextPrimary,
    )

val WinLiteFontFamily =
    FontFamily(
        Font(R.font.inter_medium, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_medium, FontWeight.SemiBold),
        Font(R.font.inter_medium, FontWeight.Bold),
    )

private val BaseTypography = Typography()

val WinLiteTypography =
    Typography(
        displayLarge = BaseTypography.displayLarge.copy(fontFamily = WinLiteFontFamily),
        displayMedium = BaseTypography.displayMedium.copy(fontFamily = WinLiteFontFamily),
        displaySmall = BaseTypography.displaySmall.copy(fontFamily = WinLiteFontFamily),
        headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = WinLiteFontFamily),
        headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = WinLiteFontFamily),
        headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = WinLiteFontFamily),
        titleLarge = BaseTypography.titleLarge.copy(fontFamily = WinLiteFontFamily),
        titleMedium = BaseTypography.titleMedium.copy(fontFamily = WinLiteFontFamily),
        titleSmall = BaseTypography.titleSmall.copy(fontFamily = WinLiteFontFamily),
        bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = WinLiteFontFamily),
        bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = WinLiteFontFamily),
        bodySmall = BaseTypography.bodySmall.copy(fontFamily = WinLiteFontFamily),
        labelLarge = BaseTypography.labelLarge.copy(fontFamily = WinLiteFontFamily),
        labelMedium = BaseTypography.labelMedium.copy(fontFamily = WinLiteFontFamily),
        labelSmall = BaseTypography.labelSmall.copy(fontFamily = WinLiteFontFamily),
    )

@Composable
fun WinLiteTheme(
    colorScheme: ColorScheme = WinLiteColorScheme,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = WinLiteTypography,
        shapes = WinLiteShapes,
        content = content,
    )
}
