package com.quirozsolutions.catalogo1boton.infra.work


import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class WorkScheduler(private val context: Context) {

    /**
     * Política debounce: subir tras X segundos sin cambios (30-60s recomendado). :contentReference[oaicite:11]{index=11}
     */
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
            .setInitialDelay(45, TimeUnit.SECONDS) // Debounce “sin cambios”
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE, // REPLACE = “sobrescribe” la cola anterior
            req
        )
    }
}
