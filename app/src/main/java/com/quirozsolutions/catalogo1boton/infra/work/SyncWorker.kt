package com.quirozsolutions.catalogo1boton.infra.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quirozsolutions.catalogo1boton.App
import com.google.android.gms.auth.api.signin.GoogleSignIn

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as App
        val client = inputData.getString(KEY_CLIENT).orEmpty()
        val sharedFolderId = inputData.getString(KEY_SHARED_FOLDER_ID)?.takeIf { it.isNotBlank() }

        if (client.isBlank()) return Result.failure()

        // 1) Construir backup latest (local)
        val backupZip = app.container.backupManager.buildLatestBackup(client)

        // 2) Subir a Drive (si hay sesión)
        val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            ?: return Result.retry()

        return try {
            app.container.driveSyncManager.uploadLatestBackup(
                account = account,
                backupZip = backupZip,
                clientName = client,
                sharedFolderId = sharedFolderId
            )
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME = "catalogo_latest_sync"
        const val KEY_CLIENT = "client"
        const val KEY_SHARED_FOLDER_ID = "sharedFolderId"
    }
}
