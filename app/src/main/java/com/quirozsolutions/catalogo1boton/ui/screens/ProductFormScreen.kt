@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.quirozsolutions.catalogo1boton.ui.screens


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.quirozsolutions.catalogo1boton.AppContainer
import com.quirozsolutions.catalogo1boton.domain.model.Product
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun ProductFormScreen(
    container: AppContainer,
    productId: String?,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var priceText by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId) {
        if (productId != null) {
            val p = container.productRepository.getById(productId) ?: return@LaunchedEffect
            priceText = (p.priceCents / 100.0).toString()
            desc = p.description.orEmpty()
            imagePath = p.imagePath
        }
    }

    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    val takePicture = rememberLauncherForActivityResult(TakePicture()) { ok ->
        if (ok) {
            val f = tempCameraFile ?: return@rememberLauncherForActivityResult
            imagePath = container.imageStore.saveFromFile(f)
            runCatching { f.delete() }
        }
    }

    val pickImage = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        if (uri != null) imagePath = container.imageStore.saveFromUri(uri)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (productId == null) "Nuevo producto" else "Editar producto") }) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Precio (obligatorio)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Row {
                Button(onClick = {
                    val f = container.imageStore.newTempCameraFile()
                    tempCameraFile = f
                    val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", f)
                    takePicture.launch(uri)
                }) { Text("Cámara") }

                Spacer(Modifier.width(12.dp))

                Button(onClick = { pickImage.launch("image/*") }) { Text("Galería") }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val img = imagePath
                    if (img.isNullOrBlank()) return@Button

                    val price = priceText.replace(",", ".").toDoubleOrNull()
                    if (price == null || price < 0) return@Button

                    scope.launch {
                        val now = System.currentTimeMillis()
                        val id = productId ?: UUID.randomUUID().toString()
                        val product = Product(
                            id = id,
                            priceCents = (price * 100).toLong(),
                            description = desc.takeIf { it.isNotBlank() },
                            imagePath = img,
                            createdAt = if (productId == null) now else (container.productRepository.getById(id)?.createdAt ?: now),
                            updatedAt = now
                        )
                        container.productRepository.upsert(product)

                        // Debounced sync (anti-saturación) :contentReference[oaicite:14]{index=14}
                        container.workScheduler.scheduleDebouncedSync(clientName = "cliente", sharedFolderId = null)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
        }
    }
}
