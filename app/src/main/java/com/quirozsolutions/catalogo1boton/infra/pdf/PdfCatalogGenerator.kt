package com.quirozsolutions.catalogo1boton.infra.pdf

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.quirozsolutions.catalogo1boton.domain.model.CatalogTemplate
import com.quirozsolutions.catalogo1boton.domain.model.Product
import com.quirozsolutions.catalogo1boton.infra.files.ImageStore
import com.quirozsolutions.catalogo1boton.infra.pdf.templates.TemplateRegistry
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

        val tpl = TemplateRegistry.resolve(template)

        // Portada opcional
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

        val perPage = tpl.itemsPerPage()
        val cols = tpl.columns()

        while (index < products.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber++).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            val items = products.subList(index, minOf(index + perPage, products.size))
            val rows = (items.size + cols - 1) / cols

            val margin = 24
            val cellW = (pageW - margin * 2) / cols
            val cellH = (pageH - margin * 2) / maxOf(rows, 1)

            items.forEachIndexed { i, p ->
                val col = i % cols
                val row = i / cols

                val left = margin + col * cellW
                val top = margin + row * cellH

                // el -8 lo conservamos para no pegar al borde
                val rect = android.graphics.Rect(left, top, left + cellW - 8, top + cellH - 8)

                val bmp = imageStore.loadBitmap(
                    p.imagePath,
                    maxSidePx = tpl.imageMaxSidePx()
                )

                val price = money.format(p.priceCents / 100.0)

                tpl.drawItem(
                    canvas = canvas,
                    rect = rect,
                    product = p,
                    image = bmp,
                    priceText = price
                )
            }

            doc.finishPage(page)
            index += perPage
        }

        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        return outFile
    }
}
