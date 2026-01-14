package com.quirozsolutions.catalogo1boton.infra.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import java.io.File

class DriveSyncManager(
    private val context: Context,
    private val auth: GoogleAuthManager
) {
    fun uploadLatestBackup(
        account: GoogleSignInAccount,
        backupZip: File,
        clientName: String,
        sharedFolderId: String?
    ) {
        // TODO: implementar cuando tengamos Drive listo.
        // Por ahora, no hacemos nada para que el proyecto compile.
    }
}
