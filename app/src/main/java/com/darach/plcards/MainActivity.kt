package com.darach.plcards

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.darach.plcards.ui.common.LoadingScreen
import com.darach.plcards.ui.navigation.AppNavigation
import com.darach.plcards.ui.theme.PLCardsTheme
import com.darach.plcards.worker.SimpleDataSyncWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        clearAndEnqueueDataSync()

        enableEdgeToEdge()
        setContent {
            val settingsState by mainViewModel.settings.collectAsState()
            val loadingState by mainViewModel.loadingState.collectAsState()

            PLCardsTheme(
                darkTheme = settingsState.isDarkTheme,
                dynamicColor = settingsState.useDynamicColor && settingsState.isWc2002Mode != true,
                isWc2002Mode = settingsState.isWc2002Mode == true
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (loadingState.isFirstLaunch && (loadingState.isDataLoading || loadingState.hasError)) {
                        LoadingScreen(
                            loadingState = loadingState,
                            onRetry = { mainViewModel.retryDataLoading() }
                        )
                    } else {
                        val windowSizeClass = calculateWindowSizeClass(this)
                        AppNavigation(windowSizeClass)
                    }
                }
            }
        }
    }

    private fun clearAndEnqueueDataSync() {
        try {
            val workManager = WorkManager.getInstance(this)
            workManager.cancelAllWork()

            val workRequest = OneTimeWorkRequestBuilder<SimpleDataSyncWorker>().build()
            workManager.enqueueUniqueWork(
                "initial_data_sync",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up data sync: ${e.message}", e)
        }
    }
}