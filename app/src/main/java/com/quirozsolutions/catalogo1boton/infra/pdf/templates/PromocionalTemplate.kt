package com.quirozsolutions.catalogo1boton.infra.pdf.templates

import android.graphics.*
import com.quirozsolutions.catalogo1boton.domain.model.Product
import com.quirozsolutions.catalogo1boton.domain.model.displayName

class PromocionalTemplate : PdfTemplate {

    override fun columns() = 1

    override fun itemsPerPage() = 4

    override fun imageMaxSidePx() = 1200

    override fun drawItem(
        canvas: Canvas,
        rect: Rect,
        product: Product,
        image: Bitmap?,
        priceText: String
    ) {
        val pad = 18
        val r = Rect(
            rect.left + pad,
            rect.top + pad,
            rect.right - pad,
            rect.bottom - pad
        )

        // Fondo promocional
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFF3E0")
        }
        canvas.drawRect(r, bg)

        // Imagen grande arriba
        val imgH = (r.height() * 0.55f).toInt()
        val imgRect = Rect(r.left + 10, r.top + 10, r.right - 10, r.top + imgH)
        image?.let { canvas.drawBitmap(it, null, imgRect, null) }

        // Nombre
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#BF360C")
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(
            product.displayName.take(40),
            (r.left + 12).toFloat(),
            (imgRect.bottom + 32).toFloat(),
            namePaint
        )

        // Precio MUY grande
        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D32F2F")
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(
            priceText,
            (r.left + 12).toFloat(),
            (r.bottom - 24).toFloat(),
            pricePaint
        )
    }
}
