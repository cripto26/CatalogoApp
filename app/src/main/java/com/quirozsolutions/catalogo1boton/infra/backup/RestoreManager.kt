package com.quirozsolutions.catalogo1boton.infra.backup

import android.content.Context
import com.quirozsolutions.catalogo1boton.data.repo.ProductRepository
import com.quirozsolutions.catalogo1boton.domain.model.BackupPayload
import com.quirozsolutions.catalogo1boton.domain.model.Product
import com.quirozsolutions.catalogo1boton.infra.files.ImageStore
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import java.util.zip.ZipFile

class RestoreManager(
    private val context: Context,
    private val imageStore: ImageStore,
    private val productRepo: ProductRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun restoreFromZip(zipFile: File) {
        ZipFile(zipFile).use { zf ->
            val payloadEntry = zf.getEntry("payload.json") ?: error("payload.json no existe")
            val payloadText = zf.getInputStream(payloadEntry).bufferedReader().readText()
            val payload = json.decodeFromString(BackupPayload.serializer(), payloadText)

            payload.products.forEach { bp ->
                val imgEntry = zf.getEntry("images/${bp.imageFileName}")
                val imagePath = if (imgEntry != null) {
                    // Guarda la imagen en el almacenamiento interno de la app
                    val tmp = File(context.cacheDir, "restore_${UUID.randomUUID()}.jpg")
                    zf.getInputStream(imgEntry).use { input -> tmp.outputStream().use { input.copyTo(it) } }
                    imageStore.saveFromFile(tmp).also { tmp.delete() }
                } else {
                    // Si falta imagen, conserva placeholder (MVP: se puede manejar mejor)
                    ""
                }

                val now = System.currentTimeMillis()
                val product = Product(
                    id = bp.id,
                    priceCents = bp.priceCents,
                    description = bp.description,
                    imagePath = imagePath,
                    createdAt = bp.createdAt,
                    updatedAt = maxOf(bp.updatedAt, now)
                )
                productRepo.upsert(product)
            }
        }
    }
}
