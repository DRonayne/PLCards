package com.darach.plcards.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.plcards.data.repository.SettingsRepository
import com.darach.plcards.domain.model.SortOrder
import com.darach.plcards.util.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private var wc2002TapCounter = 0
    private var lastTapTime = 0L

    init {
        analyticsHelper.trackScreenView("Settings")
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settings.collect { appSettings ->
                _uiState.value = SettingsUiState(
                    isDarkTheme = appSettings.isDarkTheme,
                    useDynamicColor = appSettings.useDynamicColor,
                    isWc2002Mode = appSettings.isWc2002Mode ?: false,
                    defaultSortOrder = appSettings.defaultSortOrder,
                )
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsEvent.SetDarkTheme -> {
                    analyticsHelper.trackSettingChanged("dark_theme", event.enabled.toString())
                    settingsRepository.setDarkTheme(event.enabled)
                }
                is SettingsEvent.SetDynamicColor -> {
                    analyticsHelper.trackSettingChanged("dynamic_color", event.enabled.toString())
                    settingsRepository.setDynamicColor(event.enabled)
                }
                is SettingsEvent.FootballTapped -> {
                    val currentTime = System.currentTimeMillis()
                    
                    // Reset count if more than 2 seconds have passed
                    if (currentTime - lastTapTime > 2000) {
                        wc2002TapCounter = 0
                    }
                    
                    wc2002TapCounter++
                    lastTapTime = currentTime
                    
                    // Update UI state to trigger animation
                    _uiState.value = _uiState.value.copy(
                        shouldAnimateFootball = true,
                        footballTapsRemaining = maxOf(0, 3 - wc2002TapCounter)
                    )
                    
                    // Reset animation flag after a short delay
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(300)
                        _uiState.value = _uiState.value.copy(shouldAnimateFootball = false)
                    }
                    
                    if (wc2002TapCounter >= 3 && !_uiState.value.isWc2002Mode) {
                        analyticsHelper.trackWc2002ModeToggled(true)
                        settingsRepository.setWc2002Mode(true)
                        wc2002TapCounter = 0
                    }
                }
                is SettingsEvent.SetDefaultSortOrder -> {
                    analyticsHelper.trackSettingChanged("default_sort_order", event.sortOrder.name)
                    settingsRepository.setDefaultSortOrder(event.sortOrder)
                }
                is SettingsEvent.SetWc2002Mode -> {
                    analyticsHelper.trackWc2002ModeToggled(event.enabled)
                    settingsRepository.setWc2002Mode(event.enabled)
                }
            }
        }
    }
}

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val useDynamicColor: Boolean = true,
    val isWc2002Mode: Boolean = false,
    val defaultSortOrder: SortOrder = SortOrder.SEASON_NEWEST,
    val footballTapsRemaining: Int = 3,
    val shouldAnimateFootball: Boolean = false
)

sealed class SettingsEvent {
    data class SetDarkTheme(val enabled: Boolean) : SettingsEvent()
    data class SetDynamicColor(val enabled: Boolean) : SettingsEvent()
    object FootballTapped : SettingsEvent()
    data class SetWc2002Mode(val enabled: Boolean) : SettingsEvent()
    data class SetDefaultSortOrder(val sortOrder: SortOrder) : SettingsEvent()
}