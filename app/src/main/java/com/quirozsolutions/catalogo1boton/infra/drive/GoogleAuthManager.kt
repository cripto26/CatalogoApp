package com.quirozsolutions.catalogo1boton.infra.drive


import android.content.Context
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

class GoogleAuthManager(private val context: Context) {

    // Para MVP: DRIVE_FILE suele ser suficiente (archivos creados/abiertos por la app).
    // Si necesitas listar/editar por toda la unidad o carpetas compartidas de forma amplia,
    // quizá requieras DRIVE (más estricto).
    private val scopes = listOf(
        Scope(DriveScopes.DRIVE_FILE)
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
