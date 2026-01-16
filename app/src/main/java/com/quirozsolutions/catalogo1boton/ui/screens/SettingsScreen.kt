package com.quirozsolutions.catalogo1boton.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.quirozsolutions.catalogo1boton.AppContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    onBack: () -> Unit,
    syncNow: suspend (clientName: String, sharedFolderId: String?) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var clientName by remember { mutableStateOf("cliente") }
    var sharedFolderId by remember { mutableStateOf("") }

    // Estado de sesión (se recalcula al entrar y después de login)
    var signedEmail by remember { mutableStateOf<String?>(null) }

    fun refreshSession() {
        val acc = container.authManager.lastSignedInAccount()
        signedEmail = acc?.email
    }

    LaunchedEffect(Unit) {
        refreshSession()
    }

    // Launcher Google Sign-In (manejo correcto)
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            signedEmail = account.email
            Toast.makeText(ctx, "Sesión iniciada: ${account.email}", Toast.LENGTH_LONG).show()
        } catch (e: ApiException) {
            // Aquí verás el código real del problema (ej: 10 si falta SHA-1)
            Toast.makeText(
                ctx,
                "Google Sign-In falló (code=${e.statusCode})",
                Toast.LENGTH_LONG
            ).show()
            refreshSession()
        } catch (e: Exception) {
            Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            refreshSession()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ajustes") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text("Nombre cliente (para backup)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sharedFolderId,
                onValueChange = { sharedFolderId = it },
                label = { Text("Shared Folder ID (Drive) (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = if (signedEmail != null) "Sesión: $signedEmail" else "Sesión: No iniciada"
            )

            Button(
                onClick = {
                    googleSignInLauncher.launch(
                        container.authManager.signInClient().signInIntent
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión con Google (1 vez)")
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val acc = container.authManager.lastSignedInAccount()
                            if (acc == null) {
                                Toast.makeText(ctx, "Inicia sesión primero", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            val folderIdOrNull = sharedFolderId.trim().ifBlank { null }

                            syncNow(
                                clientName.trim().ifBlank { "cliente" },
                                folderIdOrNull
                            )

                            Toast.makeText(ctx, "Sincronización lanzada", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Error sincronizando: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = signedEmail != null
            ) {
                Text("Sincronizar ahora")
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}
