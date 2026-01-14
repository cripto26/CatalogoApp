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

    val picker = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            status = "Restaurando..."
            try {
                val tmp = File(ctx.cacheDir, "import_backup.zip")
                ctx.contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input)
                    tmp.outputStream().use { input.copyTo(it) }
                }
                container.restoreManager.restoreFromZip(tmp)
                status = "✅ Restauración completa"
            } catch (e: Exception) {
                status = "❌ Error: ${e.message}"
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Restaurar") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text(status)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { picker.launch("application/zip") }, modifier = Modifier.fillMaxWidth()) {
                Text("Elegir backup ZIP")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
        }
    }
}
