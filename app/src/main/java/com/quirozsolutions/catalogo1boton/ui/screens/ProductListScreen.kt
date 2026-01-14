@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.quirozsolutions.catalogo1boton.ui.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.quirozsolutions.catalogo1boton.AppContainer
import com.quirozsolutions.catalogo1boton.domain.model.Product
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductListScreen(
    container: AppContainer,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onGenerate: () -> Unit,
    onSettings: () -> Unit,
    onRestore: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val products by container.productRepository.observeAll().collectAsState(initial = emptyList())
    val money = remember { NumberFormat.getCurrencyInstance(Locale("es", "CO")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                actions = {
                    TextButton(onClick = onRestore) { Text("Restaurar") }
                    TextButton(onClick = onSettings) { Text("Ajustes") }
                }
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(onClick = onGenerate) { Text("PDF") }
                Spacer(Modifier.height(12.dp))
                FloatingActionButton(onClick = onAdd) { Text("+") }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            items(products) { p ->
                ProductRow(
                    p = p,
                    price = money.format(p.priceCents / 100.0),
                    onClick = { onEdit(p.id) },
                    onDelete = {
                        scope.launch {
                            val prod = container.productRepository.getById(p.id) ?: return@launch
                            container.productRepository.deleteById(p.id)
                            container.imageStore.delete(prod.imagePath)

                            // Debounced sync (anti-saturación) :contentReference[oaicite:13]{index=13}
                            // (clientName/sharedFolderId salen de Settings; por MVP usa "cliente")
                            container.workScheduler.scheduleDebouncedSync(clientName = "cliente", sharedFolderId = null)
                        }
                    }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ProductRow(
    p: Product,
    price: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.padding(12.dp).fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp)) {
            AsyncImage(model = p.imagePath, contentDescription = null, modifier = Modifier.size(72.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(price, style = MaterialTheme.typography.titleMedium)
                Text(p.description ?: "", style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onDelete) { Text("Eliminar") }
        }
    }
}
