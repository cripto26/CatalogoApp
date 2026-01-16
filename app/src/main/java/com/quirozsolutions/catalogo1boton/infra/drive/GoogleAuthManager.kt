package com.quirozsolutions.catalogo1boton.infra.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

class GoogleAuthManager(private val context: Context) {

    // Guardar backup en la carpeta oculta de la app en Drive (appDataFolder)
    private val scopes = listOf(
        Scope(DriveScopes.DRIVE_APPDATA)
    )

    fun signInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(scopes.first(), *scopes.drop(1).toTypedArray())
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun lastSignedInAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)
}
