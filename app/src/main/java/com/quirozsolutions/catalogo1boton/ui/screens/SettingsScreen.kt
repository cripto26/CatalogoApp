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

    var sellerWhatsapp by remember { mutableStateOf("") }
    var sellerInstagram by remember { mutableStateOf("") }
    var storeLogoPath by remember { mutableStateOf("") }

    // sesión
    var signedEmail by remember { mutableStateOf<String?>(null) }
    fun refreshSession() {
        val acc = container.authManager.lastSignedInAccount()
        signedEmail = acc?.email
    }

    // Cargar prefs al entrar
    LaunchedEffect(Unit) {
        refreshSession()
        clientName = container.appState.clientNameValue()
        sharedFolderId = container.appState.sharedFolderIdValue()
        sellerWhatsapp = container.appState.sellerWhatsappValue()
        sellerInstagram = container.appState.sellerInstagramValue()
        storeLogoPath = container.appState.storeLogoPathValue()
    }

    // ✅ Launcher Google Sign-In (como tu versión que sí funcionaba)
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            signedEmail = account.email
            Toast.makeText(ctx, "Sesión iniciada: ${account.email}", Toast.LENGTH_LONG).show()
        } catch (e: ApiException) {
            // clave para debug: si sale code=10 normalmente es SHA-1 / OAuth mal configurado
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

    // Picker logo
    val pickLogo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val path = container.imageStore.saveFromUri(uri)
                storeLogoPath = path
                container.appState.setStoreLogoPath(path)
                Toast.makeText(ctx, "Logo guardado ✅", Toast.LENGTH_SHORT).show()
            } catch (t: Throwable) {
                Toast.makeText(ctx, "No se pudo guardar el logo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ajustes") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = clientName,
                onValueChange = {
                    clientName = it
                    container.appState.setClientName(it)
                },
                label = { Text("Nombre cliente (para backup)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sharedFolderId,
                onValueChange = {
                    sharedFolderId = it
                    container.appState.setSharedFolderId(it)
                },
                label = { Text("Shared Folder ID (Drive) (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            OutlinedTextField(
                value = sellerWhatsapp,
                onValueChange = {
                    sellerWhatsapp = it
                    container.appState.setSellerWhatsapp(it)
                },
                label = { Text("WhatsApp vendedor (para la portada)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sellerInstagram,
                onValueChange = {
                    sellerInstagram = it
                    container.appState.setSellerInstagram(it)
                },
                label = { Text("Instagram vendedor (para la portada)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { pickLogo.launch("image/*") }) {
                    Text("Elegir logo")
                }
                Text(
                    text = if (storeLogoPath.isBlank()) "Sin logo" else "Logo ✅",
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Divider()

            Text("Sesión: ${signedEmail ?: "(no iniciada)"}")

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
                            val name = clientName.trim().ifBlank { "cliente" }

                            syncNow(name, folderIdOrNull)
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

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}
