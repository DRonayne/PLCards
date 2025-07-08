package com.darach.plcards.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val lightWC2002Scheme = lightColorScheme(
    primary = primaryWC2002Light,
    onPrimary = onPrimaryWC2002Light,
    primaryContainer = primaryContainerWC2002Light,
    onPrimaryContainer = onPrimaryContainerWC2002Light,
    secondary = secondaryWC2002Light,
    onSecondary = onSecondaryWC2002Light,
    secondaryContainer = secondaryContainerWC2002Light,
    onSecondaryContainer = onSecondaryContainerWC2002Light,
    tertiary = tertiaryWC2002Light,
    onTertiary = onTertiaryWC2002Light,
    tertiaryContainer = tertiaryContainerWC2002Light,
    onTertiaryContainer = onTertiaryContainerWC2002Light,
    error = errorWC2002Light,
    onError = onErrorWC2002Light,
    errorContainer = errorContainerWC2002Light,
    onErrorContainer = onErrorContainerWC2002Light,
    background = backgroundWC2002Light,
    onBackground = onBackgroundWC2002Light,
    surface = surfaceWC2002Light,
    onSurface = onSurfaceWC2002Light,
    surfaceVariant = surfaceVariantWC2002Light,
    onSurfaceVariant = onSurfaceVariantWC2002Light,
    outline = outlineWC2002Light,
    outlineVariant = outlineVariantWC2002Light,
    scrim = scrimWC2002Light,
    inverseSurface = inverseSurfaceWC2002Light,
    inverseOnSurface = inverseOnSurfaceWC2002Light,
    inversePrimary = inversePrimaryWC2002Light,
    surfaceDim = surfaceDimWC2002Light,
    surfaceBright = surfaceBrightWC2002Light,
    surfaceContainerLowest = surfaceContainerLowestWC2002Light,
    surfaceContainerLow = surfaceContainerLowWC2002Light,
    surfaceContainer = surfaceContainerWC2002Light,
    surfaceContainerHigh = surfaceContainerHighWC2002Light,
    surfaceContainerHighest = surfaceContainerHighestWC2002Light,
)

private val darkWC2002Scheme = darkColorScheme(
    primary = primaryWC2002Dark,
    onPrimary = onPrimaryWC2002Dark,
    primaryContainer = primaryContainerWC2002Dark,
    onPrimaryContainer = onPrimaryContainerWC2002Dark,
    secondary = secondaryWC2002Dark,
    onSecondary = onSecondaryWC2002Dark,
    secondaryContainer = secondaryContainerWC2002Dark,
    onSecondaryContainer = onSecondaryContainerWC2002Dark,
    tertiary = tertiaryWC2002Dark,
    onTertiary = onTertiaryWC2002Dark,
    tertiaryContainer = tertiaryContainerWC2002Dark,
    onTertiaryContainer = onTertiaryContainerWC2002Dark,
    error = errorWC2002Dark,
    onError = onErrorWC2002Dark,
    errorContainer = errorContainerWC2002Dark,
    onErrorContainer = onErrorContainerWC2002Dark,
    background = backgroundWC2002Dark,
    onBackground = onBackgroundWC2002Dark,
    surface = surfaceWC2002Dark,
    onSurface = onSurfaceWC2002Dark,
    surfaceVariant = surfaceVariantWC2002Dark,
    onSurfaceVariant = onSurfaceVariantWC2002Dark,
    outline = outlineWC2002Dark,
    outlineVariant = outlineVariantWC2002Dark,
    scrim = scrimWC2002Dark,
    inverseSurface = inverseSurfaceWC2002Dark,
    inverseOnSurface = inverseOnSurfaceWC2002Dark,
    inversePrimary = inversePrimaryWC2002Dark,
    surfaceDim = surfaceDimWC2002Dark,
    surfaceBright = surfaceBrightWC2002Dark,
    surfaceContainerLowest = surfaceContainerLowestWC2002Dark,
    surfaceContainerLow = surfaceContainerLowWC2002Dark,
    surfaceContainer = surfaceContainerWC2002Dark,
    surfaceContainerHigh = surfaceContainerHighWC2002Dark,
    surfaceContainerHighest = surfaceContainerHighestWC2002Dark,
)


@Composable
fun PLCardsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    isWc2002Mode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isWc2002Mode -> if (darkTheme) darkWC2002Scheme else lightWC2002Scheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    val typography = if (isWc2002Mode) WC2002Typography else AppTypography

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@Composable
fun ThemeShowcase(themeName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "$themeName Theme Showcase",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Color Swatches
        ColorSwatch(
            "Primary",
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
        ColorSwatch(
            "Primary Container",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        ColorSwatch(
            "Secondary",
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.onSecondary
        )
        ColorSwatch(
            "Secondary Container",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        ColorSwatch(
            "Tertiary",
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary
        )
        ColorSwatch(
            "Tertiary Container",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        ColorSwatch("Error", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
        ColorSwatch(
            "Background",
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.onBackground
        )
        ColorSwatch(
            "Surface",
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // UI Elements
        Text(
            text = "UI Elements",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* Do something */ },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Primary Button")
        }

        FilledTonalButton(
            onClick = { /* Do something */ },
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text("Secondary Tonal Button")
        }

        OutlinedButton(
            onClick = { /* Do something */ },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Tertiary Outlined Button")
        }

        var checkedState by remember { mutableStateOf(true) }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = checkedState, onCheckedChange = { checkedState = it })
            Text("Checkbox Example", color = MaterialTheme.colorScheme.onSurface)
        }

        var sliderPosition by remember { mutableStateOf(0.5f) }
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )

        var switchState by remember { mutableStateOf(true) }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Switch Example", color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(8.dp))
            Switch(checked = switchState, onCheckedChange = { switchState = it })
        }


        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Text(
                text = "Card Example",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Text(
                text = "Elevated Card Example",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ColorSwatch(name: String, color: Color, onColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(color),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            color = onColor,
            modifier = Modifier.padding(start = 8.dp)
        )
        Text(
            text = color.toString(),
            color = onColor,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Preview(showBackground = true, name = "PLCardsTheme Light")
@Composable
fun PLCardsLightThemePreview() {
    PLCardsTheme(darkTheme = false, dynamicColor = false) {
        ThemeShowcase("PLCardsTheme Light")
    }
}

@Preview(showBackground = true, name = "PLCardsTheme Dark")
@Composable
fun PLCardsDarkThemePreview() {
    PLCardsTheme(darkTheme = true, dynamicColor = false) {
        ThemeShowcase("PLCardsTheme Dark")
    }
}

@Preview(showBackground = true, name = "PLCardsTheme WC2002 Light")
@Composable
fun PLCardsWC2002LightThemePreview() {
    PLCardsTheme(darkTheme = false, isWc2002Mode = true) {
        ThemeShowcase("PLCardsTheme WC2002 Light")
    }
}

@Preview(showBackground = true, name = "PLCardsTheme WC2002 Dark")
@Composable
fun PLCardsWC2002DarkThemePreview() {
    PLCardsTheme(darkTheme = true, isWc2002Mode = true) {
        ThemeShowcase("PLCardsTheme WC2002 Dark")
    }
}

@Preview(showBackground = true, name = "PLCardsTheme Dynamic Light")
@Composable
fun PLCardsDynamicLightThemePreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PLCardsTheme(darkTheme = false, dynamicColor = true) {
            ThemeShowcase("PLCardsTheme Dynamic Light")
        }
    } else {
        Text("Dynamic Color not available on this API level")
    }
}

@Preview(showBackground = true, name = "PLCardsTheme Dynamic Dark")
@Composable
fun PLCardsDynamicDarkThemePreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PLCardsTheme(darkTheme = true, dynamicColor = true) {
            ThemeShowcase("PLCardsTheme Dynamic Dark")
        }
    } else {
        Text("Dynamic Color not available on this API level")
    }
}