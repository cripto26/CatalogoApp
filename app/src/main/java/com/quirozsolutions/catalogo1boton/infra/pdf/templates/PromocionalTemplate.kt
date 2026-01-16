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
        val r = Rect(rect.left + pad, rect.top + pad, rect.right - pad, rect.bottom - pad)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.parseColor("#6A1B9A")
        }
        canvas.drawRect(r, bg)
        canvas.drawRect(r, border)

        // Badge arriba-izq
        val badgeRect = Rect(r.left + 12, r.top + 12, r.left + 160, r.top + 52)
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#6A1B9A") }
        canvas.drawRect(badgeRect, badgePaint)

        val badgeText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("OFERTA", (badgeRect.left + 18).toFloat(), (badgeRect.bottom - 14).toFloat(), badgeText)

        // Imagen grande
        val imgRect = Rect(r.left + 16, r.top + 70, r.right - 16, r.top + 330)
        image?.let { canvas.drawBitmap(it, null, imgRect, null) }

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
        }
        val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#333333")
            textSize = 16f
        }
        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6A1B9A")
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
        }

        val name = product.displayName.take(38)
        val desc = (product.description ?: "").take(140)

        canvas.drawText(name, (r.left + 16).toFloat(), (r.bottom - 120).toFloat(), namePaint)
        canvas.drawText(desc, (r.left + 16).toFloat(), (r.bottom - 86).toFloat(), descPaint)
        canvas.drawText(priceText, (r.left + 16).toFloat(), (r.bottom - 28).toFloat(), pricePaint)
    }
}
