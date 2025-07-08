package com.darach.plcards.ui.settings

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.SingletonImageLoader
import com.darach.plcards.BuildConfig
import com.darach.plcards.R
import com.darach.plcards.domain.model.SortOrder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isTablet = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    val maxWidth = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Medium -> 600.dp
        WindowWidthSizeClass.Expanded -> 800.dp
        else -> Int.MAX_VALUE.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .let { if (isTablet) it.widthIn(max = maxWidth) else it.fillMaxWidth() }
                .padding(if (isTablet) 32.dp else 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(if (isTablet) 20.dp else 16.dp)
        ) {
            // App Title
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Logo and WC2002 Mode Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Display appropriate logo based on mode
                    Image(
                        painter = painterResource(
                            id = if (uiState.isWc2002Mode) R.drawable.wc2002_logo else R.drawable.retro_pl
                        ),
                        contentDescription = if (uiState.isWc2002Mode) "WC2002 Logo" else "Premier League Logo",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = if (!uiState.isWc2002Mode) {
                            ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        } else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interactive football for WC2002 mode
                    val scale by animateFloatAsState(
                        targetValue = if (uiState.shouldAnimateFootball) 1.2f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(scale)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.onEvent(SettingsEvent.FootballTapped)

                                if (uiState.footballTapsRemaining == 0 && !uiState.isWc2002Mode) {
                                    scope.launch {
                                        Toast.makeText(
                                            context,
                                            "WC2002 Mode Activated! ⚽",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.fevernova_football),
                            contentDescription = "Fevernova Football - Tap 3 times to enable WC2002 mode",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (!uiState.isWc2002Mode) {
                            "Tap the ball ${uiState.footballTapsRemaining} more time${if (uiState.footballTapsRemaining != 1) "s" else ""} to enable WC2002 mode"
                        } else {
                            "WC2002 Mode Active"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dark Mode Setting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Use dark theme throughout the app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = uiState.isDarkTheme,
                    onCheckedChange = { enabled ->
                        viewModel.onEvent(SettingsEvent.SetDarkTheme(enabled))
                    }
                )
            }

            // Dynamic Color Setting
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (uiState.isWc2002Mode) Modifier else Modifier
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Use Dynamic Color (Material You)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.isWc2002Mode) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = if (uiState.isWc2002Mode) {
                            "Disabled in WC2002 Mode"
                        } else {
                            "Adapt colors to your wallpaper"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = uiState.useDynamicColor && !uiState.isWc2002Mode,
                    enabled = !uiState.isWc2002Mode,
                    onCheckedChange = { enabled ->
                        if (!uiState.isWc2002Mode) {
                            viewModel.onEvent(SettingsEvent.SetDynamicColor(enabled))
                        }
                    }
                )
            }

            // WC2002 Mode Setting (shown when activated)
            if (uiState.isWc2002Mode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WC2002 Mode",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Experience the retro 2002 World Cup theme",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                            checked = uiState.isWc2002Mode,
                            onCheckedChange = { enabled ->
                                viewModel.onEvent(SettingsEvent.SetWc2002Mode(enabled))
                                if (!enabled) {
                                    Toast.makeText(
                                        context,
                                        "WC2002 Mode Deactivated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Default Sort Order Dropdown
            var sortDropdownExpanded by remember { mutableStateOf(false) }
            val sortOptions = SortOrder.entries

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Default Sort Order",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Choose default sorting for search results",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = sortDropdownExpanded,
                    onExpandedChange = { sortDropdownExpanded = !sortDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.defaultSortOrder.displayName,
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = sortDropdownExpanded,
                        onDismissRequest = { sortDropdownExpanded = false }
                    ) {
                        sortOptions.forEach { sortOrder ->
                            DropdownMenuItem(
                                text = { Text(sortOrder.displayName) },
                                onClick = {
                                    viewModel.onEvent(SettingsEvent.SetDefaultSortOrder(sortOrder))
                                    sortDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clear Cache Button
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val imageLoader = SingletonImageLoader.get(context)
                            imageLoader.memoryCache?.clear()
                            imageLoader.diskCache?.clear()
                            Toast.makeText(context, "Image cache cleared", Toast.LENGTH_SHORT)
                                .show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to clear cache", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Image Cache")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About This App
            var showAboutDialog by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAboutDialog = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "About This App",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "App information and credits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    title = { Text("About PLCards") },
                    text = {
                        Column {
                            Text("PLCards v${BuildConfig.VERSION_NAME}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("A premium football card collection app featuring cards from various seasons and leagues.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Developed with modern Android technologies including Jetpack Compose, Hilt, and Room.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("For support or feedback, please contact us through the app store.")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            // App Version
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onEvent(SettingsEvent.FootballTapped) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "App Version",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))

        }
    }
}