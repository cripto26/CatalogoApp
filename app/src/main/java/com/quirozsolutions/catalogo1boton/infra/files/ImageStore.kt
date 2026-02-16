package com.quirozsolutions.catalogo1boton.infra.files

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

class ImageStore(private val context: Context) {

    private val imagesDir: File
        get() = File(context.filesDir, "images").apply { mkdirs() }

    fun newTempCameraFile(): File =
        File(context.cacheDir, "camera_${UUID.randomUUID()}.jpg")

    /**
     * Guarda una imagen ENTRANTE (galería) ya optimizada:
     * - Re-muestrea a un maxSide razonable (para no guardar 4000x3000).
     * - Rota por EXIF (para no quedar girada en PDF).
     * - Comprime en JPEG con calidad fija.
     *
     * Esto NO solo ayuda al PDF; también baja peso de backups/sync.
     */
    fun saveFromUri(uri: Uri): String {
        val id = UUID.randomUUID().toString()
        val outFile = File(imagesDir, "$id.jpg")

        // Si no se puede optimizar (por ejemplo, stream raro), se cae a copia directa.
        return runCatching {
            optimizeAndSaveFromUri(uri, outFile)
            outFile.absolutePath
        }.getOrElse {
            context.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "No se pudo abrir el URI de imagen" }
                FileOutputStream(outFile).use { output -> input.copyTo(output) }
            }
            outFile.absolutePath
        }
    }

    /**
     * Guarda una imagen ENTRANTE (archivo, típico cámara) ya optimizada.
     */
    fun saveFromFile(file: File): String {
        val id = UUID.randomUUID().toString()
        val outFile = File(imagesDir, "$id.jpg")

        return runCatching {
            optimizeAndSaveFromFile(file, outFile)
            outFile.absolutePath
        }.getOrElse {
            file.copyTo(outFile, overwrite = true)
            outFile.absolutePath
        }
    }

    /**
     * Carga bitmap genérico (por compatibilidad).
     */
    fun loadBitmap(path: String, maxSidePx: Int = DEFAULT_LOAD_MAX_SIDE_PX): Bitmap? {
        val file = File(path)
        if (!file.exists()) return null

        val decoded = decodeScaledBitmapFromFile(path, maxSidePx) ?: return null
        val rotated = rotateIfNeeded(decoded, path)
        return rotated
    }

    /**
     * Carga bitmap ESPECÍFICO para PDF:
     * - Decodifica con inSampleSize según el tamaño que necesitas.
     * - Si aun queda un poco grande, escala EXACTO hacia abajo.
     * - Rota por EXIF.
     *
     * El peso del PDF baja MUCHO porque estás metiendo menos píxeles por imagen.
     */
    fun loadBitmapForPdf(path: String, targetMaxSidePx: Int): Bitmap? {
        val file = File(path)
        if (!file.exists()) return null

        val safeTarget = targetMaxSidePx.coerceIn(120, 2400)

        val decoded = decodeScaledBitmapFromFile(path, safeTarget) ?: return null
        val rotated = rotateIfNeeded(decoded, path)

        // Escala exacta hacia abajo si todavía supera el target (inSampleSize solo baja en potencias de 2)
        val maxSide = max(rotated.width, rotated.height)
        if (maxSide <= safeTarget) return rotated

        val scale = safeTarget.toFloat() / maxSide.toFloat()
        val newW = (rotated.width * scale).roundToInt().coerceAtLeast(1)
        val newH = (rotated.height * scale).roundToInt().coerceAtLeast(1)

        val scaled = Bitmap.createScaledBitmap(rotated, newW, newH, true)
        if (scaled !== rotated) rotated.recycle()
        return scaled
    }

    fun delete(path: String) {
        runCatching { File(path).delete() }
    }

    // -------------------------
    // Optimización al guardar
    // -------------------------

    private fun optimizeAndSaveFromUri(uri: Uri, outFile: File) {
        val cr = context.contentResolver

        // 1) Leer EXIF (rotación)
        val rotationDegrees = cr.openInputStream(uri).use { input ->
            if (input == null) 0 else readRotationDegrees(input)
        }

        // 2) Leer bounds
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        cr.openInputStream(uri).use { input ->
            requireNotNull(input) { "No se pudo abrir el URI de imagen" }
            BitmapFactory.decodeStream(input, null, bounds)
        }

        val inSample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, SAVE_MAX_SIDE_PX)

        // 3) Decodificar ya muestreada
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = inSample
            inPreferredConfig = Bitmap.Config.RGB_565 // reduce memoria
        }

        val bmp = cr.openInputStream(uri).use { input ->
            requireNotNull(input) { "No se pudo abrir el URI de imagen" }
            BitmapFactory.decodeStream(input, null, decodeOpts)
        } ?: throw IllegalStateException("No se pudo decodificar la imagen")

        // 4) Rotar si aplica
        val rotated = rotateBitmap(bmp, rotationDegrees)

        // 5) Guardar JPEG comprimido
        FileOutputStream(outFile).use { fos ->
            rotated.compress(Bitmap.CompressFormat.JPEG, SAVE_JPEG_QUALITY, fos)
        }

        if (rotated !== bmp) bmp.recycle()
        rotated.recycle()
    }

    private fun optimizeAndSaveFromFile(inputFile: File, outFile: File) {
        val rotationDegrees = runCatching {
            ExifInterface(inputFile.absolutePath).let { exif ->
                exifOrientationToDegrees(
                    exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                )
            }
        }.getOrDefault(0)

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(inputFile.absolutePath, bounds)

        val inSample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, SAVE_MAX_SIDE_PX)

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = inSample
            inPreferredConfig = Bitmap.Config.RGB_565
        }

        val bmp = BitmapFactory.decodeFile(inputFile.absolutePath, decodeOpts)
            ?: throw IllegalStateException("No se pudo decodificar la imagen")

        val rotated = rotateBitmap(bmp, rotationDegrees)

        FileOutputStream(outFile).use { fos ->
            rotated.compress(Bitmap.CompressFormat.JPEG, SAVE_JPEG_QUALITY, fos)
        }

        if (rotated !== bmp) bmp.recycle()
        rotated.recycle()
    }

    // -------------------------
    // Decode helpers
    // -------------------------

    private fun decodeScaledBitmapFromFile(path: String, maxSidePx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)

        val inSample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxSidePx)

        val opts = BitmapFactory.Options().apply {
            inSampleSize = inSample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeFile(path, opts)
    }

    private fun calculateInSampleSize(w: Int, h: Int, maxSide: Int): Int {
        if (w <= 0 || h <= 0) return 1
        var inSample = 1
        var halfW = w / 2
        var halfH = h / 2
        while (halfW / inSample >= maxSide && halfH / inSample >= maxSide) {
            inSample *= 2
        }
        return inSample.coerceAtLeast(1)
    }

    // -------------------------
    // EXIF rotation
    // -------------------------

    private fun rotateIfNeeded(bmp: Bitmap, path: String): Bitmap {
        val degrees = runCatching {
            val exif = ExifInterface(path)
            exifOrientationToDegrees(
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            )
        }.getOrDefault(0)

        return rotateBitmap(bmp, degrees)
    }

    private fun readRotationDegrees(inputStream: java.io.InputStream): Int {
        return runCatching {
            val exif = ExifInterface(inputStream)
            exifOrientationToDegrees(
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            )
        }.getOrDefault(0)
    }

    private fun exifOrientationToDegrees(orientation: Int): Int {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    private fun rotateBitmap(src: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return src
        val m = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }

    companion object {
        // Tamaño máximo al GUARDAR imágenes en tu storage interno.
        // Súbelo/bájalo si quieres más/menos detalle en general.
        private const val SAVE_MAX_SIDE_PX = 1600

        // Compresión JPEG al GUARDAR en storage interno.
        private const val SAVE_JPEG_QUALITY = 82

        // Default por compatibilidad (no PDF).
        private const val DEFAULT_LOAD_MAX_SIDE_PX = 900
    }
}
