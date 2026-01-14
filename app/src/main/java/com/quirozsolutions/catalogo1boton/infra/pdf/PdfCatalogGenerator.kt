package com.quirozsolutions.catalogo1boton.infra.pdf


import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.quirozsolutions.catalogo1boton.domain.model.CatalogTemplate
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
    private val money = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    fun generate(
        products: List<Product>,
        template: CatalogTemplate,
        businessTitle: String?,
        contactLine: String?
    ): File {
        val outDir = File(context.filesDir, "catalogs").apply { mkdirs() }
        val outFile = File(outDir, "catalog_${System.currentTimeMillis()}.pdf")

        val doc = PdfDocument()
        val pageW = 595  // A4 aprox (pt)
        val pageH = 842

        var index = 0
        var pageNumber = 1

        // Portada opcional (según propuesta)
        if (!businessTitle.isNullOrBlank() || !contactLine.isNullOrBlank()) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber++).create()
            val page = doc.startPage(pageInfo)
            val c = page.canvas

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 16f }

            c.drawText(businessTitle ?: "Catálogo", 40f, 120f, titlePaint)
            if (!contactLine.isNullOrBlank()) c.drawText(contactLine, 40f, 160f, subPaint)

            doc.finishPage(page)
        }

        val perPage = when (template) {
            CatalogTemplate.MINIMALISTA -> 6
            CatalogTemplate.PROMOCIONAL -> 4
            CatalogTemplate.COMPACTA -> 9
        }

        while (index < products.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber++).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f }
            val paintPrice = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.LTGRAY
            }

            val items = products.subList(index, minOf(index + perPage, products.size))

            // Layout simple por grilla
            val cols = when (template) {
                CatalogTemplate.MINIMALISTA -> 2
                CatalogTemplate.PROMOCIONAL -> 1
                CatalogTemplate.COMPACTA -> 3
            }
            val rows = (items.size + cols - 1) / cols

            val margin = 24
            val cellW = (pageW - margin * 2) / cols
            val cellH = (pageH - margin * 2) / maxOf(rows, 1)

            items.forEachIndexed { i, p ->
                val col = i % cols
                val row = i / cols

                val left = margin + col * cellW
                val top = margin + row * cellH
                val rect = Rect(left, top, left + cellW - 8, top + cellH - 8)

                canvas.drawRect(rect, border)

                // Imagen
                val bmp = imageStore.loadBitmap(p.imagePath, maxSidePx = if (template == CatalogTemplate.PROMOCIONAL) 1200 else 700)
                val imgRect = Rect(rect.left + 10, rect.top + 10, rect.right - 10, rect.top + (rect.height() * 0.6f).toInt())
                if (bmp != null) {
                    canvas.drawBitmap(bmp, null, imgRect, null)
                }

                // Texto
                val price = money.format(p.priceCents / 100.0)
                val desc = (p.description ?: "").take(120)

                val textY = imgRect.bottom + 18f
                canvas.drawText(price, rect.left + 10f, textY, paintPrice)
                canvas.drawText(desc, rect.left + 10f, textY + 18f, paintText)
            }

            doc.finishPage(page)
            index += perPage
        }

        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        return outFile
    }
}
