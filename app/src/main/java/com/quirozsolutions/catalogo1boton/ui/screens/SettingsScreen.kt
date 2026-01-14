@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.quirozsolutions.catalogo1boton.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.quirozsolutions.catalogo1boton.AppContainer

@Composable
fun SettingsScreen(container: AppContainer, onBack: () -> Unit) {
    val ctx = LocalContext.current

    var clientName by remember { mutableStateOf("cliente") }
    var sharedFolderId by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        // sesión queda guardada en GoogleSignIn.getLastSignedInAccount(ctx)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Ajustes") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text("Nombre cliente (para backup)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = sharedFolderId,
                onValueChange = { sharedFolderId = it },
                label = { Text("Shared Folder ID (Drive) (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val intent = container.authManager.signInClient().signInIntent
                    launcher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Iniciar sesión con Google (1 vez)") }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val acc = GoogleSignIn.getLastSignedInAccount(ctx) ?: return@Button
                    container.workScheduler.scheduleDebouncedSync(
                        clientName = clientName,
                        sharedFolderId = sharedFolderId.ifBlank { null }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sincronizar ahora") }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
        }
    }
}
