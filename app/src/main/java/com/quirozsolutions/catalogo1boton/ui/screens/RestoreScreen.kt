@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.quirozsolutions.catalogo1boton.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quirozsolutions.catalogo1boton.AppContainer
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun RestoreScreen(container: AppContainer, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf("Selecciona un ZIP de backup...") }
    var loading by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            loading = true
            status = "Restaurando desde archivo..."
            try {
                val tmp = File(ctx.cacheDir, "import_backup.zip")
                ctx.contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input)
                    tmp.outputStream().use { output -> input.copyTo(output) }
                }
                container.restoreManager.restoreFromZip(tmp)
                status = "✅ Restauración completa (archivo)"
            } catch (e: Exception) {
                status = "❌ Error: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun restoreFromDrive() {
        scope.launch {
            loading = true
            status = "Buscando backup en Google Drive..."
            try {
                val account = container.authManager.lastSignedInAccount()
                    ?: throw IllegalStateException("No hay sesión iniciada con Google")

                // Por defecto: busca en Mi unidad
                val sharedFolderId: String? = null

                val zip = container.driveSyncManager.downloadLatestBackup(
                    account = account,
                    sharedFolderId = sharedFolderId
                ) ?: throw IllegalStateException("No se encontró backup en Drive")

                status = "Restaurando desde Google Drive..."
                container.restoreManager.restoreFromZip(zip)

                status = "✅ Restauración completa (Drive)"
            } catch (e: Exception) {
                status = "❌ Error: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Restaurar") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text(status)

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { picker.launch("application/zip") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text("Elegir backup ZIP")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { restoreFromDrive() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text("Restaurar desde Google Drive")
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) { Text("Volver") }
        }
    }
}
