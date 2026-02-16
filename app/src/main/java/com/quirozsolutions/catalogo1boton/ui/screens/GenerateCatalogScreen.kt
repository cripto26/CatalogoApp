package com.quirozsolutions.catalogo1boton.ui.screens

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.quirozsolutions.catalogo1boton.AppContainer
import com.quirozsolutions.catalogo1boton.infra.pdf.sharePdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateCatalogScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val handler = remember { Handler(Looper.getMainLooper()) }

    var debugSlots by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    var progress by remember { mutableFloatStateOf(0f) }
    var currentStep by remember { mutableIntStateOf(0) }
    var totalSteps by remember { mutableIntStateOf(1) }

    fun updateProgress(cur: Int, total: Int) {
        currentStep = cur.coerceAtLeast(0)
        totalSteps = total.coerceAtLeast(1)
        progress = (currentStep.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    }

    if (isGenerating) {
        AlertDialog(
            onDismissRequest = { /* bloqueado mientras genera */ },
            confirmButton = {},
            title = { Text("Generando PDF…") },
            text = {
                Column {
                    // ✅ overload moderno (no deprecated)
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    val pct = (progress * 100f).toInt().coerceIn(0, 100)
                    Text("$pct%  ($currentStep/$totalSteps)")
                    Spacer(Modifier.height(6.dp))
                    Text("Por favor espera, esto puede tardar unos segundos.")
                }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Generar catálogo") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dibujar slots (debug)")
                Switch(checked = debugSlots, onCheckedChange = { debugSlots = it })
            }

            Spacer(Modifier.height(12.dp))

            Button(
                enabled = !isGenerating,
                onClick = {
                    scope.launch {
                        isGenerating = true
                        updateProgress(0, 1)

                        try {
                            // Traer datos
                            val products = withContext(Dispatchers.IO) {
                                container.productRepository.getAllOnce()
                            }

                            val logoPath = withContext(Dispatchers.IO) {
                                container.appState.storeLogoPathValue().ifBlank { null }
                            }
                            val ws = withContext(Dispatchers.IO) { container.appState.sellerWhatsappValue() }
                            val ig = withContext(Dispatchers.IO) { container.appState.sellerInstagramValue() }

                            // Generar PDF (con progreso real por producto)
                            val pdf = withContext(Dispatchers.Default) {
                                container.pdfGenerator.generateMinimalistaCatalog(
                                    products = products,
                                    storeLogoPath = logoPath,
                                    sellerWhatsapp = ws,
                                    sellerInstagram = ig,
                                    debugDrawSlots = debugSlots,
                                    onProgress = { cur, total ->
                                        // El generador corre en background: actualizar UI en Main thread
                                        handler.post { updateProgress(cur, total) }
                                    }
                                )
                            }

                            isGenerating = false
                            sharePdf(ctx, pdf)

                            // ✅ Cada PDF nuevo -> subir backup inmediatamente
                            container.appState.scheduleSyncNowOrDebounced(debounced = false)

                        } catch (e: Exception) {
                            isGenerating = false
                            snackbarHostState.showSnackbar(
                                message = "Error generando PDF: ${e.message ?: "desconocido"}"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generar PDF (Minimalista + Portada)")
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onBack) {
                Text("Volver")
            }
        }
    }
}
