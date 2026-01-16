package com.quirozsolutions.catalogo1boton.infra.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.quirozsolutions.catalogo1boton.App

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as App

        val clientName = inputData.getString(KEY_CLIENT).orEmpty()
        val sharedFolderId = inputData.getString(KEY_SHARED_FOLDER_ID)?.takeIf { it.isNotBlank() }

        if (clientName.isBlank()) return Result.failure()

        val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            ?: return Result.retry()

        return try {
            val zip = app.container.backupManager.buildLatestBackup(clientName)

            app.container.driveSyncManager.uploadLatestBackup(
                account = account,
                backupZip = zip,
                clientName = clientName,
                sharedFolderId = sharedFolderId
            )

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME = "catalogo_latest_sync"
        const val KEY_CLIENT = "client"
        const val KEY_SHARED_FOLDER_ID = "sharedFolderId"
    }
}
