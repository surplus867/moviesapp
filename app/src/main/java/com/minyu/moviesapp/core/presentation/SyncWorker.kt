// File: SyncWorker.kt
// Purpose: simple WorkManager CoroutineWorker that performs a background sync and shows a notification.

package com.minyu.moviesapp.core.presentation

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.minyu.moviesapp.core.NotificationHelper
import kotlinx.coroutines.delay

/**
 * Minimal background sync worker.
 * Replace the simulated delay with real network/db calls.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // Requires POST_NOTIFICATIONS on Android 13+ if you want to show notifications.
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            Log.d("SyncWorker", "Starting background sync")

            // Simulate work - swap with repository/network calls
            delay(2000)

            // Safe to call repeatedly; consider creating channels once at app startup
            NotificationHelper.createChannel(applicationContext)
            NotificationHelper.showNotification(
                applicationContext,
                id = 1001,
                title = "MoviesApp",
                message = "Background sync completed"
            )

            Log.d("SyncWorker", "Background sync completed")
            Result.success()
        } catch (t: Throwable) {
            Log.e("SyncWorker", "Sync failed", t)
            // Retry for transient failures
            Result.retry()
        }
    }
}