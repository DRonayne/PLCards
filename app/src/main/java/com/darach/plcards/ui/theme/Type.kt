package com.darach.plcards.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.darach.plcards.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Source Sans 3"),
        fontProvider = provider,
    )
)

val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Poppins"),
        fontProvider = provider,
    )
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)

val bodyWC2002FontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Source Serif 4"),
        fontProvider = provider,
    )
)

val displayWC2002FontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Playfair Display"),
        fontProvider = provider,
    )
)

// Default Material 3 typography values
val baselineWC2002 = Typography()

val WC2002Typography = Typography(
    displayLarge = baselineWC2002.displayLarge.copy(fontFamily = displayWC2002FontFamily),
    displayMedium = baselineWC2002.displayMedium.copy(fontFamily = displayWC2002FontFamily),
    displaySmall = baselineWC2002.displaySmall.copy(fontFamily = displayWC2002FontFamily),
    headlineLarge = baselineWC2002.headlineLarge.copy(fontFamily = displayWC2002FontFamily),
    headlineMedium = baselineWC2002.headlineMedium.copy(fontFamily = displayWC2002FontFamily),
    headlineSmall = baselineWC2002.headlineSmall.copy(fontFamily = displayWC2002FontFamily),
    titleLarge = baselineWC2002.titleLarge.copy(fontFamily = displayWC2002FontFamily),
    titleMedium = baselineWC2002.titleMedium.copy(fontFamily = displayWC2002FontFamily),
    titleSmall = baselineWC2002.titleSmall.copy(fontFamily = displayWC2002FontFamily),
    bodyLarge = baselineWC2002.bodyLarge.copy(fontFamily = bodyWC2002FontFamily),
    bodyMedium = baselineWC2002.bodyMedium.copy(fontFamily = bodyWC2002FontFamily),
    bodySmall = baselineWC2002.bodySmall.copy(fontFamily = bodyWC2002FontFamily),
    labelLarge = baselineWC2002.labelLarge.copy(fontFamily = bodyWC2002FontFamily),
    labelMedium = baselineWC2002.labelMedium.copy(fontFamily = bodyWC2002FontFamily),
    labelSmall = baselineWC2002.labelSmall.copy(fontFamily = bodyWC2002FontFamily),
)

