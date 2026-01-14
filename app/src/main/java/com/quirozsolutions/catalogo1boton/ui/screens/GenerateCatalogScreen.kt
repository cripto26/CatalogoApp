@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.quirozsolutions.catalogo1boton.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quirozsolutions.catalogo1boton.AppContainer
import com.quirozsolutions.catalogo1boton.domain.model.CatalogTemplate
import com.quirozsolutions.catalogo1boton.infra.pdf.sharePdf
import kotlinx.coroutines.launch

@Composable
fun GenerateCatalogScreen(container: AppContainer, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var template by remember { mutableStateOf(CatalogTemplate.MINIMALISTA) }
    var business by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Generar catálogo") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {

            Text("Plantilla")
            Spacer(Modifier.height(8.dp))
            TemplatePicker(template = template, onChange = { template = it })

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = business,
                onValueChange = { business = it },
                label = { Text("Nombre del negocio (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Contacto/redes (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        val products = container.productRepository.getAllOnce()
                        val pdf = container.pdfGenerator.generate(
                            products = products,
                            template = template,
                            businessTitle = business.takeIf { it.isNotBlank() },
                            contactLine = contact.takeIf { it.isNotBlank() }
                        )
                        // ✅ Aquí se arregla: sharePdf necesita un Context real
                        sharePdf(ctx, pdf)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Generar y compartir PDF (1 botón)") }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
        }
    }
}

@Composable
private fun TemplatePicker(template: CatalogTemplate, onChange: (CatalogTemplate) -> Unit) {
    Column {
        CatalogTemplate.values().forEach { t ->
            Row {
                RadioButton(selected = (template == t), onClick = { onChange(t) })
                Text(t.name)
            }
        }
    }
}
