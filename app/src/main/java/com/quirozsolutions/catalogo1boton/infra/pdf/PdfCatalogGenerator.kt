package com.quirozsolutions.catalogo1boton.infra.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.annotation.WorkerThread
import com.quirozsolutions.catalogo1boton.domain.model.Product
import com.quirozsolutions.catalogo1boton.infra.files.ImageStore
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

class PdfCatalogGenerator(
    private val context: Context,
    private val imageStore: ImageStore
) {
    private val pageW = 595
    private val pageH = 842

    private val money = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    @WorkerThread
    fun generateWithPngTemplate(
        products: List<Product>,
        pageAssetPath: String,
        slots: List<Rect>,
        debugDrawSlots: Boolean = false
    ): File {
        val outDir = File(context.filesDir, "catalogs").apply { mkdirs() }
        val outFile = File(outDir, "catalog_${System.currentTimeMillis()}.pdf")

        val doc = PdfDocument()
        val bg = loadAssetBitmap(pageAssetPath)

        val perPage = slots.size
        var idx = 0
        var pageNumber = 1

        while (idx < products.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber++).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            // Fondo
            if (bg != null) {
                canvas.drawBitmap(bg, null, Rect(0, 0, pageW, pageH), null)
            } else {
                canvas.drawColor(Color.WHITE)
            }

            // Debug slots (bordes rojos)
            if (debugDrawSlots) {
                val dbgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = Color.RED
                }
                slots.forEach { canvas.drawRect(it, dbgPaint) }
            }

            val slice = products.subList(idx, minOf(idx + perPage, products.size))

            slice.forEachIndexed { i, p ->
                val slot = slots[i]
                val bmp = imageStore.loadBitmap(p.imagePath, maxSidePx = 1400)

                val price = money.format(p.priceCents / 100.0)
                val name = (p.description ?: p.id).trim()

                drawProductCard(
                    canvas = canvas,
                    slot = slot,
                    image = bmp,
                    priceText = price,
                    nameText = name
                )
            }

            doc.finishPage(page)
            idx += perPage
        }

        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        return outFile
    }

    private fun drawProductCard(
        canvas: Canvas,
        slot: Rect,
        image: Bitmap?,
        priceText: String,
        nameText: String
    ) {
        // Deja un pequeño padding para que no toque el borde del marco del PNG
        val pad = 12
        val inner = Rect(slot.left + pad, slot.top + pad, slot.right - pad, slot.bottom - pad)

        // Imagen arriba (zona grande)
        val imgH = (inner.height() * 0.72f).toInt()
        val imgRect = Rect(inner.left, inner.top, inner.right, inner.top + imgH)

        // Texto abajo
        val textRect = Rect(inner.left, imgRect.bottom + 6, inner.right, inner.bottom)

        // Imagen sin deformar
        if (image != null) {
            drawBitmapCenterCrop(canvas, image, imgRect)
        }

        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(76, 175, 80) // verde
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 152, 0) // naranja
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Ajuste fino de baseline
        canvas.drawText(priceText, textRect.left.toFloat(), (textRect.top + 18).toFloat(), pricePaint)
        canvas.drawText(nameText.take(32), textRect.left.toFloat(), (textRect.top + 36).toFloat(), namePaint)
    }

    private fun drawBitmapCenterCrop(canvas: Canvas, bitmap: Bitmap, target: Rect) {
        val srcW = bitmap.width
        val srcH = bitmap.height
        if (srcW <= 0 || srcH <= 0) return

        val srcRatio = srcW.toFloat() / srcH.toFloat()
        val dstRatio = target.width().toFloat() / target.height().toFloat()

        val srcRect = if (srcRatio > dstRatio) {
            val newW = (srcH * dstRatio).toInt()
            val x = (srcW - newW) / 2
            Rect(x, 0, x + newW, srcH)
        } else {
            val newH = (srcW / dstRatio).toInt()
            val y = (srcH - newH) / 2
            Rect(0, y, srcW, y + newH)
        }

        canvas.drawBitmap(bitmap, srcRect, target, null)
    }

    private fun loadAssetBitmap(assetPath: String): Bitmap? {
        return runCatching {
            context.assets.open(assetPath).use { input ->
                BitmapFactory.decodeStream(input)
            }
        }.getOrNull()
    }
}
