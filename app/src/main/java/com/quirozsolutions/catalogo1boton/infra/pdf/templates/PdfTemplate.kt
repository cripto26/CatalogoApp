package com.quirozsolutions.catalogo1boton.infra.pdf.templates

import android.graphics.*
import com.quirozsolutions.catalogo1boton.domain.model.Product
import com.quirozsolutions.catalogo1boton.domain.model.displayName

class PrecioGrandeTemplate : PdfTemplate {

    override fun columns() = 2
    override fun itemsPerPage() = 6
    override fun imageMaxSidePx(): Int = 900

    override fun drawItem(
        canvas: Canvas,
        rect: Rect,
        product: Product,
        image: Bitmap?,
        priceText: String
    ) {
        val r = Rect(rect.left + 10, rect.top + 10, rect.right - 10, rect.bottom - 10)

        // Fondo blanco + sombra falsa (borde doble)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#E0E0E0")
        }
        canvas.drawRect(r, bg)
        canvas.drawRect(r, border)

        // Imagen arriba
        val imgRect = Rect(r.left + 10, r.top + 10, r.right - 10, r.top + 210)
        image?.let { canvas.drawBitmap(it, null, imgRect, null) }

        // Nombre
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(product.displayName.take(32), (r.left + 12).toFloat(), (imgRect.bottom + 28).toFloat(), namePaint)

        // Descripción
        val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#444444")
            textSize = 13f
        }
        canvas.drawText((product.description ?: "").take(90), (r.left + 12).toFloat(), (imgRect.bottom + 50).toFloat(), descPaint)

        // Precio grande abajo
        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6A1B9A")
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(priceText, (r.left + 12).toFloat(), (r.bottom - 18).toFloat(), pricePaint)
    }
}
