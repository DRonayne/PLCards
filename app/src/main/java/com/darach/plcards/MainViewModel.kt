package com.darach.plcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asFlow
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.darach.plcards.data.repository.AppSettings
import com.darach.plcards.data.repository.SettingsRepository
import com.darach.plcards.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppLoadingState(
    val isFirstLaunch: Boolean = true,
    val isDataLoading: Boolean = false,
    val dataLoadingProgress: Float = 0f,
    val dataLoadingMessage: String = "Preparing your cards...",
    val hasError: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val cardRepository: CardRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _settings = MutableStateFlow(
        AppSettings(
            isDarkTheme = false,
            useDynamicColor = true,
            isWc2002Mode = false
        )
    )
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _loadingState = MutableStateFlow(AppLoadingState())
    val loadingState: StateFlow<AppLoadingState> = _loadingState.asStateFlow()

    init {
        loadSettings()
        checkDataLoadingStatus()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settings.collect { appSettings ->
                _settings.value = appSettings
            }
        }
    }

    private fun checkDataLoadingStatus() {
        viewModelScope.launch {
            // Check if we have any cards in the database
            val cardCount = cardRepository.getCardCount()
            val isFirstLaunch = cardCount == 0

            _loadingState.value = _loadingState.value.copy(
                isFirstLaunch = isFirstLaunch,
                isDataLoading = isFirstLaunch
            )

            if (isFirstLaunch) {
                monitorDataSyncProgress()
            }
        }
    }

    private fun monitorDataSyncProgress() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkLiveData("initial_data_sync")
                .asFlow()
                .collect { workInfos ->
                    val workInfo = workInfos.firstOrNull()

                    when (workInfo?.state) {
                        WorkInfo.State.RUNNING -> {
                            _loadingState.value = _loadingState.value.copy(
                                isDataLoading = true,
                                dataLoadingProgress = 0.5f,
                                dataLoadingMessage = "Downloading latest cards...",
                                hasError = false
                            )
                        }

                        WorkInfo.State.SUCCEEDED -> {
                            _loadingState.value = _loadingState.value.copy(
                                isDataLoading = false,
                                dataLoadingProgress = 1f,
                                dataLoadingMessage = "Cards loaded successfully!",
                                hasError = false
                            )
                        }

                        WorkInfo.State.FAILED -> {
                            _loadingState.value = _loadingState.value.copy(
                                isDataLoading = false,
                                dataLoadingProgress = 0f,
                                dataLoadingMessage = "Failed to load cards",
                                hasError = true,
                                errorMessage = "Unable to download card data. Please check your internet connection."
                            )
                        }

                        WorkInfo.State.ENQUEUED -> {
                            _loadingState.value = _loadingState.value.copy(
                                isDataLoading = true,
                                dataLoadingProgress = 0.1f,
                                dataLoadingMessage = "Preparing download...",
                                hasError = false
                            )
                        }

                        else -> {
                            // No work or cancelled - check if we have data now
                            val cardCount = cardRepository.getCardCount()
                            if (cardCount > 0) {
                                _loadingState.value = _loadingState.value.copy(
                                    isFirstLaunch = false,
                                    isDataLoading = false,
                                    dataLoadingProgress = 1f
                                )
                            }
                        }
                    }
                }
        }
    }

    fun retryDataLoading() {
        viewModelScope.launch {
            _loadingState.value = _loadingState.value.copy(
                hasError = false,
                errorMessage = null,
                isDataLoading = true,
                dataLoadingProgress = 0f,
                dataLoadingMessage = "Retrying download..."
            )

            // Re-trigger the data sync by checking status again
            checkDataLoadingStatus()
        }
    }
}