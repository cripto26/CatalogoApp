package com.quirozsolutions.catalogo1boton.infra.drive

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
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
    private val backupsFolderName = "Catalogo 1 Botón Backups"
    private val latestBackupName = "catalogo_latest.zip"

    private fun driveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
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

    private suspend fun findFolderId(drive: Drive, folderName: String, parentId: String): String? =
        withContext(Dispatchers.IO) {
            val q =
                "mimeType='application/vnd.google-apps.folder' and name='${folderName}' and trashed=false and '${parentId}' in parents"

            val list: FileList = drive.files().list()
                .setSpaces("drive")
                .setQ(q)
                .setFields("files(id,name)")
                .execute()

            list.files?.firstOrNull()?.id
        }

    private suspend fun createFolder(drive: Drive, folderName: String, parentId: String): String =
        withContext(Dispatchers.IO) {
            val metadata = com.google.api.services.drive.model.File().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
                parents = listOf(parentId)
            }

            drive.files().create(metadata)
                .setFields("id")
                .execute()
                .id
        }

    private suspend fun ensureBackupsFolder(drive: Drive, sharedFolderId: String?): String =
        withContext(Dispatchers.IO) {
            val parent = sharedFolderId?.takeIf { it.isNotBlank() } ?: "root"

            val existingId = findFolderId(drive, backupsFolderName, parent)
            existingId ?: createFolder(drive, backupsFolderName, parent)
        }

    private suspend fun findFileInFolder(drive: Drive, fileName: String, folderId: String): String? =
        withContext(Dispatchers.IO) {
            val q = "name='${fileName}' and trashed=false and '${folderId}' in parents"

            val list: FileList = drive.files().list()
                .setSpaces("drive")
                .setQ(q)
                .setFields("files(id,name,modifiedTime)")
                .execute()

            list.files
                ?.maxByOrNull { it.modifiedTime?.value ?: 0L }
                ?.id
        }

    suspend fun uploadLatestBackup(
        account: GoogleSignInAccount,
        backupZip: File,
        clientName: String,
        sharedFolderId: String?
    ) = withContext(Dispatchers.IO) {
        Log.d("DRIVE_SYNC", "uploadLatestBackup() llamado")
        Log.d("DRIVE_SYNC", "Account: ${account.email}")
        Log.d("DRIVE_SYNC", "ZIP existe: ${backupZip.exists()} tamaño=${backupZip.length()}")
        Log.d("DRIVE_SYNC", "sharedFolderId=$sharedFolderId")

        val drive = driveService(account)

        Log.d("DRIVE_SYNC", "Buscando / creando carpeta en Drive...")
        val folderId = ensureBackupsFolder(drive, sharedFolderId)
        Log.d("DRIVE_SYNC", "FolderId obtenido: $folderId")

        val existingId = findFileInFolder(drive, latestBackupName, folderId)

        val metadata = com.google.api.services.drive.model.File().apply {
            name = latestBackupName
            parents = listOf(folderId)
            description = "Último backup de $clientName"
        }

        val mediaContent = FileContent("application/zip", backupZip)

        val result = if (existingId == null) {
            Log.d("DRIVE_SYNC", "No existe backup previo -> creando archivo")
            drive.files().create(metadata, mediaContent)
                .setFields("id")
                .execute()
        } else {
            Log.d("DRIVE_SYNC", "Existe backup previo -> actualizando archivo id=$existingId")
            drive.files().update(existingId, metadata, mediaContent)
                .setFields("id")
                .execute()
        }

        Log.d("DRIVE_SYNC", "Backup subido OK. fileId=${result.id}")
    }

    suspend fun downloadLatestBackup(
        account: GoogleSignInAccount,
        sharedFolderId: String?
    ): File? = withContext(Dispatchers.IO) {
        Log.d("DRIVE_SYNC", "downloadLatestBackup() llamado")
        Log.d("DRIVE_SYNC", "Account: ${account.email}")
        Log.d("DRIVE_SYNC", "sharedFolderId=$sharedFolderId")

        val drive = driveService(account)

        val folderId = ensureBackupsFolder(drive, sharedFolderId)
        Log.d("DRIVE_SYNC", "FolderId (download): $folderId")

        val fileId = findFileInFolder(drive, latestBackupName, folderId)
            ?: run {
                Log.d("DRIVE_SYNC", "No se encontró $latestBackupName en la carpeta")
                return@withContext null
            }

        val out = File(context.cacheDir, "drive_latest_backup.zip")
        if (out.exists()) out.delete()

        Log.d("DRIVE_SYNC", "Descargando fileId=$fileId -> ${out.absolutePath}")
        FileOutputStream(out).use { output ->
            drive.files().get(fileId).executeMediaAndDownloadTo(output)
        }

        Log.d("DRIVE_SYNC", "Descarga OK")
        out
    }
}
