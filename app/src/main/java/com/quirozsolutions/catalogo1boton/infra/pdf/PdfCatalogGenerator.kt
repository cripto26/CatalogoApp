package com.quirozsolutions.catalogo1boton.infra.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.annotation.WorkerThread
import com.quirozsolutions.catalogo1boton.domain.model.Product
import com.quirozsolutions.catalogo1boton.infra.files.ImageStore
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

class PdfCatalogGenerator(
    private val context: Context,
    private val imageStore: ImageStore
) {
    private val money = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    /**
     * ✅ Catálogo minimalista
     * - Portada: assets/templates/portada_minimalista.png
     * - Páginas: assets/templates/tamplate_minimalista_minimalista.png
     */
    @WorkerThread
    fun generateMinimalistaCatalog(
        products: List<Product>,
        storeLogoPath: String?,
        sellerWhatsapp: String,
        sellerInstagram: String,
        debugDrawSlots: Boolean = false,
        coverAssetPath: String = "templates/portada_minimalista.png",
        pageAssetPath: String = "templates/tamplate_minimalista_minimalista.png",
        slots: List<PngSlot> = PngSlots.minimalista6()
    ): File {
        val outDir = File(context.filesDir, "catalogs").apply { mkdirs() }
        val outFile = File(outDir, "catalog_${System.currentTimeMillis()}.pdf")

        val doc = PdfDocument()

        // ---------- 1) PORTADA ----------
        val coverBg = loadAssetBitmap(coverAssetPath)
        val coverW = coverBg?.width ?: 1024
        val coverH = coverBg?.height ?: 761

        val pageInfoCover = PdfDocument.PageInfo.Builder(coverW, coverH, 1).create()
        val coverPage = doc.startPage(pageInfoCover)
        val coverCanvas = coverPage.canvas

        drawBackground(coverCanvas, coverBg, coverW, coverH)

        // Logo encima del texto "TU MARCA AQUÍ" (rect aprox para 1024x761)
        val logoRect = Rect(
            (coverW * 0.22f).toInt(),
            (coverH * 0.17f).toInt(),
            (coverW * 0.78f).toInt(),
            (coverH * 0.30f).toInt()
        )

        val logoBitmap = storeLogoPath
            ?.takeIf { it.isNotBlank() }
            ?.let { imageStore.loadBitmap(it, maxSidePx = 1200) }

        if (logoBitmap != null) {
            // Cubre un poco el texto de fondo por si se alcanza a ver
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; alpha = 220 }
            coverCanvas.drawRect(logoRect, bgPaint)

            drawBitmapFitCenter(coverCanvas, logoBitmap, logoRect)
        }

        // Textos redes (sobre las “píldoras”)
        val socialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(90, 90, 90)
            textSize = (coverH * 0.040f) // ~23
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
        }

        // Posiciones aproximadas (ajustables)
        val textX = (coverW * 0.44f)
        val whatsappY = (coverH * 0.61f)
        val instaY = (coverH * 0.73f)

        val ws = sellerWhatsapp.ifBlank { "XX XXXX XXXX" }
        val ig = sellerInstagram.ifBlank { "@tuinstagram" }

        coverCanvas.drawText(ws, textX, whatsappY, socialPaint)
        coverCanvas.drawText(ig, textX, instaY, socialPaint)

        doc.finishPage(coverPage)

        // ---------- 2) PÁGINAS DE PRODUCTOS ----------
        val bg = loadAssetBitmap(pageAssetPath)
        val pageW = bg?.width ?: 1024
        val pageH = bg?.height ?: 761

        val perPage = slots.size
        var idx = 0
        var pageNumber = 2

        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(60, 60, 60)
            textSize = (pageH * 0.035f) // ~26
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val dbgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.RED
        }

        while (idx < products.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber++).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            drawBackground(canvas, bg, pageW, pageH)

            val sub = products.subList(idx, min(idx + perPage, products.size))

            for (i in sub.indices) {
                val p = sub[i]
                val slot = slots[i]

                val bmp = imageStore.loadBitmap(p.imagePath, maxSidePx = 1600)
                if (bmp != null) {
                    drawBitmapCenterCrop(canvas, bmp, slot.imageRect)
                }

                val price = money.format(p.priceCents / 100.0)
                canvas.drawText(price, slot.pricePoint.x, slot.pricePoint.y, pricePaint)

                if (debugDrawSlots) {
                    canvas.drawRect(slot.imageRect, dbgPaint)
                    canvas.drawCircle(slot.pricePoint.x, slot.pricePoint.y, 6f, dbgPaint)
                }
            }

            doc.finishPage(page)
            idx += perPage
        }

        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        return outFile
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers (sin duplicados -> elimina la ambigüedad de overloads)
    // ---------------------------------------------------------------------------------------------

    private fun loadAssetBitmap(assetPath: String): Bitmap? {
        return try {
            context.assets.open(assetPath).use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun drawBackground(canvas: Canvas, bg: Bitmap?, pageW: Int, pageH: Int) {
        if (bg != null) {
            val target = Rect(0, 0, pageW, pageH)
            drawBitmapFitCenter(canvas, bg, target)
        } else {
            canvas.drawColor(Color.WHITE)
        }
    }

    /**
     * ✅ Fit-center: mantiene proporción, centra, no deforma.
     * (NOTA: solo existe esta versión -> no hay overload ambiguity)
     */
    private fun drawBitmapFitCenter(canvas: Canvas, bitmap: Bitmap, target: Rect, paint: Paint? = null) {
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        if (bw <= 0f || bh <= 0f) return

        val scale = min(target.width() / bw, target.height() / bh)
        val dw = (bw * scale).toInt()
        val dh = (bh * scale).toInt()

        val left = target.left + (target.width() - dw) / 2
        val top = target.top + (target.height() - dh) / 2

        val dst = Rect(left, top, left + dw, top + dh)
        canvas.drawBitmap(bitmap, null, dst, paint)
    }

    /**
     * ✅ Center-crop: llena el rectángulo y recorta sobrante (sin deformar).
     */
    private fun drawBitmapCenterCrop(canvas: Canvas, bitmap: Bitmap, target: Rect) {
        val srcW = bitmap.width
        val srcH = bitmap.height
        if (srcW <= 0 || srcH <= 0) return

        val srcRatio = srcW.toFloat() / srcH.toFloat()
        val dstRatio = target.width().toFloat() / target.height().toFloat()

        val srcRect = if (srcRatio > dstRatio) {
            // Recorta lados
            val newW = (srcH * dstRatio).toInt()
            val x = (srcW - newW) / 2
            Rect(x, 0, x + newW, srcH)
        } else {
            // Recorta arriba/abajo
            val newH = (srcW / dstRatio).toInt()
            val y = (srcH - newH) / 2
            Rect(0, y, srcW, y + newH)
        }

        canvas.drawBitmap(bitmap, srcRect, target, null)
    }
}
