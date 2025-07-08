package com.darach.plcards.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.darach.plcards.R
import com.darach.plcards.data.local.dao.CardDao
import com.darach.plcards.data.mappers.toCardEntity
import com.darach.plcards.data.remote.ApiService
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SimpleDataSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val cardDao: CardDao,
    private val apiService: ApiService,
    private val crashlytics: FirebaseCrashlytics
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SimpleDataSyncWorker"
        private const val CHANNEL_ID = "DATA_SYNC_CHANNEL"
        private const val NOTIFICATION_ID = 1
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Syncing Card Data")
            .setContentText("Downloading latest trading cards...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Data Sync",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows progress of card data synchronization"
            setShowBadge(false)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "SimpleDataSyncWorker starting...")
        return withContext(Dispatchers.IO) {
            try {
                // Preserve existing user data before sync
                Log.d(TAG, "Getting existing user states...")
                val existingUserStates = cardDao.getAllCardsUserState().associateBy { it.id }
                Log.d(TAG, "Found ${existingUserStates.size} existing user states")

                Log.d(TAG, "Making API call to ${ApiService.BASE_URL}api/cards")
                val response = try {
                    apiService.getAllCards()
                } catch (e: Exception) {
                    Log.e(TAG, "API call failed: ${e.message}", e)
                    crashlytics.recordException(e)
                    throw e
                }
                Log.d(TAG, "API call successful, got ${response.cards.size} cards")

                val entities = response.cards
                    .mapIndexedNotNull { index, card ->
                        try {

                            val hasPlayerName = !card.playerName.isNullOrBlank()
                            val hasCardNumber = !card.cardNumber.isNullOrBlank()
                            val hasSeason = !card.season.isNullOrBlank()

                            // Accept card if it has ANY meaningful data
                            if (hasPlayerName || hasCardNumber || hasSeason || !card.team.isNullOrBlank()) {
                                val entity = card.toCardEntity()

                                // Preserve existing user data if it exists
                                val existingState = existingUserStates[entity.id]
                                if (existingState != null) {
                                    entity.copy(
                                        isFavorite = existingState.isFavorite,
                                        positionInFormation = existingState.positionInFormation
                                    )
                                } else {
                                    entity
                                }
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mapping card $index: ${e.message}", e)
                            crashlytics.recordException(e)
                            null
                        }
                    }

                if (entities.isEmpty()) {
                    Log.w(TAG, "No valid cards found in API response")
                    return@withContext Result.failure()
                }

                try {
                    cardDao.insertAll(entities)
                    Log.d(TAG, "Successfully synced ${entities.size} cards")
                } catch (e: Exception) {
                    Log.e(TAG, "Database upsert failed: ${e.message}", e)
                    crashlytics.recordException(e)
                    throw e
                }

                return@withContext Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Error during data sync: ${e.message}", e)
                crashlytics.recordException(e)
                return@withContext Result.retry()
            }
        }
    }
}