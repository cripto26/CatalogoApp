package com.quirozsolutions.catalogo1boton.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quirozsolutions.catalogo1boton.AppContainer
import com.quirozsolutions.catalogo1boton.infra.pdf.sharePdf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateCatalogScreen(container: AppContainer, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var debugSlots by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Generar catálogo") }) }
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
                onClick = {
                    scope.launch {
                        val products = container.productRepository.getAllOnce()

                        val logoPath = container.appState.storeLogoPathValue().ifBlank { null }
                        val ws = container.appState.sellerWhatsappValue()
                        val ig = container.appState.sellerInstagramValue()

                        val pdf = container.pdfGenerator.generateMinimalistaCatalog(
                            products = products,
                            storeLogoPath = logoPath,
                            sellerWhatsapp = ws,
                            sellerInstagram = ig,
                            debugDrawSlots = debugSlots
                        )

                        sharePdf(ctx, pdf)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generar PDF (Minimalista + Portada)")
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}
