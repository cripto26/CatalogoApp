package com.quirozsolutions.catalogo1boton.infra.backup


import android.content.Context
import android.os.Build
import com.quirozsolutions.catalogo1boton.data.repo.ProductRepository
import com.quirozsolutions.catalogo1boton.domain.model.*
import com.quirozsolutions.catalogo1boton.infra.files.ImageStore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupManager(
    private val context: Context,
    private val imageStore: ImageStore,
    private val productRepo: ProductRepository
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun latestBackupFile(): File =
        File(context.filesDir, "backups/catalogo_latest.zip").apply {
            parentFile?.mkdirs()
        }

    suspend fun buildLatestBackup(clientName: String): File {
        val products = productRepo.getAllOnce()
        val meta = BackupMetadata(
            createdAt = System.currentTimeMillis(),
            clientName = clientName,
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
        )

        // Copiamos imágenes al zip con nombre estable
        val backupProducts = products.map { p ->
            val imageName = "${p.id}.jpg"
            BackupProduct(
                id = p.id,
                priceCents = p.priceCents,
                description = p.description,
                imageFileName = imageName,
                createdAt = p.createdAt,
                updatedAt = p.updatedAt
            )
        }

        // Creamos payload primero con sha vacío, luego calculamos sha final del zip
        val tempPayload = BackupPayload(meta, backupProducts, sha256 = "")
        val tempJsonBytes = json.encodeToString(tempPayload).encodeToByteArray()

        val outFile = latestBackupFile()
        if (outFile.exists()) outFile.delete()

        ZipOutputStream(BufferedOutputStream(FileOutputStream(outFile))).use { zos ->
            // metadata.json
            zos.putNextEntry(ZipEntry("payload.json"))
            zos.write(tempJsonBytes)
            zos.closeEntry()

            // images/
            backupProducts.forEach { bp ->
                val prod = products.first { it.id == bp.id }
                val imgFile = File(prod.imagePath)
                if (imgFile.exists()) {
                    zos.putNextEntry(ZipEntry("images/${bp.imageFileName}"))
                    imgFile.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }

        // Calcula sha del zip y re-escribe payload.json con sha real (simple y robusto)
        val sha = sha256(outFile)
        val finalPayload = tempPayload.copy(sha256 = sha)
        val finalJsonBytes = json.encodeToString(finalPayload).encodeToByteArray()

        // Reescribir zip: (para MVP es aceptable; si quieres optimizar, se hace zip con manifest al final)
        val rebuilt = File(context.filesDir, "backups/catalogo_latest_rebuilt.zip")
        ZipOutputStream(BufferedOutputStream(FileOutputStream(rebuilt))).use { zos ->
            zos.putNextEntry(ZipEntry("payload.json"))
            zos.write(finalJsonBytes)
            zos.closeEntry()

            // Re-empaca images/
            backupProducts.forEach { bp ->
                val prod = products.first { it.id == bp.id }
                val imgFile = File(prod.imagePath)
                if (imgFile.exists()) {
                    zos.putNextEntry(ZipEntry("images/${bp.imageFileName}"))
                    imgFile.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }

        outFile.delete()
        rebuilt.renameTo(outFile)
        return outFile
    }

    private fun sha256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(8192)
            while (true) {
                val r = input.read(buf)
                if (r <= 0) break
                md.update(buf, 0, r)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
