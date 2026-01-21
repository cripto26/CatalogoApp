@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.quirozsolutions.catalogo1boton.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quirozsolutions.catalogo1boton.AppContainer
import com.quirozsolutions.catalogo1boton.infra.pdf.PngSlots
import com.quirozsolutions.catalogo1boton.infra.pdf.sharePdf
import kotlinx.coroutines.launch

@Composable
fun GenerateCatalogScreen(container: AppContainer, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ DEBUG opcional: si lo pones true, el PDF dibuja bordes rojos de slots
    var debugSlots by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Generar catálogo") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mostrar guías (debug)")
                Switch(checked = debugSlots, onCheckedChange = { debugSlots = it })
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        val products = container.productRepository.getAllOnce()

                        val pdf = container.pdfGenerator.generateWithPngTemplate(
                            products = products,
                            pageAssetPath = "templates/ofertas_6.png",
                            slots = PngSlots.ofertas6(),
                            debugDrawSlots = debugSlots
                        )

                        sharePdf(ctx, pdf)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generar PDF (Plantilla PNG: Ofertas 6)")
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}
