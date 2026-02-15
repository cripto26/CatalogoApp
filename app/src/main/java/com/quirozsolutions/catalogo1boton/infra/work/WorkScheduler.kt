package com.quirozsolutions.catalogo1boton.infra.work

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class WorkScheduler(private val context: Context) {

    fun scheduleDebouncedSync(clientName: String, sharedFolderId: String?) {
        val data = workDataOf(
            SyncWorker.KEY_CLIENT to clientName,
            SyncWorker.KEY_SHARED_FOLDER_ID to (sharedFolderId ?: "")
        )

        val req = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(data)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .setInitialDelay(45, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            req
        )
    }

    // ✅ NUEVO: sincroniza YA (sin delay)
    fun scheduleImmediateSync(clientName: String, sharedFolderId: String?) {
        val data = workDataOf(
            SyncWorker.KEY_CLIENT to clientName,
            SyncWorker.KEY_SHARED_FOLDER_ID to (sharedFolderId ?: "")
        )

        val req = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(data)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            req
        )
    }
}
