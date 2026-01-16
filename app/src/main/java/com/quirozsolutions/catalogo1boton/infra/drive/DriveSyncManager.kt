
package com.quirozsolutions.catalogo1boton.infra.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class DriveSyncManager(
    private val context: Context,
    private val auth: GoogleAuthManager
) {
    private fun driveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Catalogo 1 Botón")
            .build()
    }

    private suspend fun findLatestBackupFile(drive: Drive, name: String): String? =
        withContext(Dispatchers.IO) {
            val list: FileList = drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='${name}' and trashed=false")
                .setFields("files(id,name,modifiedTime)")
                .execute()

            // si hay varios, toma el más reciente
            list.files
                ?.maxByOrNull { it.modifiedTime?.value ?: 0L }
                ?.id
        }

    suspend fun uploadLatestBackup(
        account: GoogleSignInAccount,
        backupZip: File
    ) = withContext(Dispatchers.IO) {
        val drive = driveService(account)
        val filename = "catalogo_latest.zip"

        val existingId = findLatestBackupFile(drive, filename)

        val metadata = com.google.api.services.drive.model.File().apply {
            name = filename
            parents = listOf("appDataFolder")
        }

        val mediaContent = FileContent("application/zip", backupZip)

        if (existingId == null) {
            // Crear
            drive.files().create(metadata, mediaContent)
                .setFields("id")
                .execute()
        } else {
            // Actualizar
            drive.files().update(existingId, metadata, mediaContent)
                .setFields("id")
                .execute()
        }
    }

    suspend fun downloadLatestBackup(
        account: GoogleSignInAccount
    ): File? = withContext(Dispatchers.IO) {
        val drive = driveService(account)
        val filename = "catalogo_latest.zip"
        val fileId = findLatestBackupFile(drive, filename) ?: return@withContext null

        val out = File(context.cacheDir, "drive_latest_backup.zip")
        if (out.exists()) out.delete()

        FileOutputStream(out).use { output ->
            drive.files().get(fileId).executeMediaAndDownloadTo(output)
        }

        out
    }
}
