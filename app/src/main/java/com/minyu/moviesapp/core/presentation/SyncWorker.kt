package com.minyu.moviesapp.core.presentation

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.minyu.moviesapp.core.NotificationHelper
import kotlinx.coroutines.delay

class SyncWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        try {
            // Example stub: replace with real sync logic (repository/network/db)
            Log.d("SyncWorker", "Starting background sync")
            // simulate network/db work
            delay(2000)
            // On success, show a notification
            NotificationHelper.createChannel(applicationContext)
            NotificationHelper.showNotification(
                applicationContext,
                id = 1001,
                title = "MoviesApp",
                message = "Background sync completed"
            )
            Log.d("SyncWorker", "Background sync completed")
            return Result.success()
        } catch (t: Throwable) {
            Log.e("SyncWorker", "Sync failed", t)
            return Result.retry()
        }
    }
}