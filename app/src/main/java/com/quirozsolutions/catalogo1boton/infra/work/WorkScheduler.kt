package com.quirozsolutions.catalogo1boton.infra.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class WorkScheduler(private val context: Context) {

    /**
     * Política debounce: subir tras X segundos sin cambios (30-60s recomendado).
     */
    fun scheduleDebouncedSync(clientName: String, sharedFolderId: String?) {
        val req = buildSyncRequest(
            clientName = clientName,
            sharedFolderId = sharedFolderId,
            initialDelaySeconds = 45L
        )

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE, // REPLACE = sobrescribe la cola anterior
            req
        )
    }

    /**
     * ✅ Sin debounce: dispara la sincronización inmediatamente.
     * Esto es lo que AppState.kt espera cuando debounced = false.
     */
    fun scheduleImmediateSync(clientName: String, sharedFolderId: String?) {
        val req = buildSyncRequest(
            clientName = clientName,
            sharedFolderId = sharedFolderId,
            initialDelaySeconds = 0L
        )

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            req
        )
    }

    private fun buildSyncRequest(
        clientName: String,
        sharedFolderId: String?,
        initialDelaySeconds: Long
    ) = OneTimeWorkRequestBuilder<SyncWorker>()
        .setInputData(
            workDataOf(
                SyncWorker.KEY_CLIENT to clientName,
                SyncWorker.KEY_SHARED_FOLDER_ID to (sharedFolderId ?: "")
            )
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .apply {
            if (initialDelaySeconds > 0) {
                setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
            }
        }
        .build()
}
