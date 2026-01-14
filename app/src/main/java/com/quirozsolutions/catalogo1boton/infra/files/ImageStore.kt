package com.quirozsolutions.catalogo1boton.infra.files


import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageStore(private val context: Context) {

    private val imagesDir: File
        get() = File(context.filesDir, "images").apply { mkdirs() }

    fun newTempCameraFile(): File =
        File(context.cacheDir, "camera_${UUID.randomUUID()}.jpg")

    fun saveFromUri(uri: Uri): String {
        val id = UUID.randomUUID().toString()
        val outFile = File(imagesDir, "$id.jpg")

        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "No se pudo abrir el URI de imagen" }
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        return outFile.absolutePath
    }

    fun saveFromFile(file: File): String {
        val id = UUID.randomUUID().toString()
        val outFile = File(imagesDir, "$id.jpg")
        file.copyTo(outFile, overwrite = true)
        return outFile.absolutePath
    }

    fun loadBitmap(path: String, maxSidePx: Int = 900): Bitmap? {
        val file = File(path)
        if (!file.exists()) return null

        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opts)

        val scale = calculateInSampleSize(opts.outWidth, opts.outHeight, maxSidePx)
        val opts2 = BitmapFactory.Options().apply { inSampleSize = scale }
        return BitmapFactory.decodeFile(path, opts2)
    }

    fun delete(path: String) {
        runCatching { File(path).delete() }
    }

    private fun calculateInSampleSize(w: Int, h: Int, maxSide: Int): Int {
        var inSample = 1
        var halfW = w / 2
        var halfH = h / 2
        while (halfW / inSample >= maxSide && halfH / inSample >= maxSide) {
            inSample *= 2
        }
        return inSample.coerceAtLeast(1)
    }
}
