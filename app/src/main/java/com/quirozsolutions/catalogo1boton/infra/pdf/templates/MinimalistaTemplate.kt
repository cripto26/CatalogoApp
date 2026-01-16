package com.quirozsolutions.catalogo1boton.infra.pdf.templates

import android.graphics.*
import com.quirozsolutions.catalogo1boton.domain.model.Product
import com.quirozsolutions.catalogo1boton.domain.model.displayName

import kotlin.math.min

class MinimalistaTemplate : PdfTemplate {

    override fun columns() = 2
    override fun itemsPerPage() = 6
    override fun imageMaxSidePx() = 700

    override fun drawItem(
        canvas: Canvas,
        rect: Rect,
        product: Product,
        image: Bitmap?,
        priceText: String
    ) {
        val pad = 14
        val r = Rect(rect.left + pad, rect.top + pad, rect.right - pad, rect.bottom - pad)

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#DDDDDD")
        }
        canvas.drawRect(r, border)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }
        val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#333333")
            textSize = 12f
        }
        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }

        // Imagen arriba (ocupando ~60%)
        val imgBottom = r.top + (r.height() * 0.60f).toInt()
        val imgRect = Rect(r.left + 10, r.top + 10, r.right - 10, imgBottom)
        image?.let { canvas.drawBitmap(it, null, imgRect, null) }

        val name = product.displayName.take(28)
        val desc = (product.description ?: "").take(110)

        val y = imgRect.bottom + 20f
        canvas.drawText(priceText, (r.left + 10).toFloat(), y, pricePaint)
        canvas.drawText(name, (r.left + 10).toFloat(), y + 18f, titlePaint)
        canvas.drawText(desc, (r.left + 10).toFloat(), y + 36f, descPaint)
    }
}
