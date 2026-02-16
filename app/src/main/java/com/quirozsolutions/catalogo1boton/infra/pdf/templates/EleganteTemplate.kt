package com.quirozsolutions.catalogo1boton.infra.pdf.templates

import android.graphics.*
import android.graphics.Typeface
import com.quirozsolutions.catalogo1boton.domain.model.Product
import com.quirozsolutions.catalogo1boton.domain.model.displayName

class EleganteTemplate : PdfTemplate {

    override fun columns() = 2
    override fun itemsPerPage() = 6

    override fun imageRect(itemRect: Rect): Rect {
        val (_, imgRect) = layout(itemRect)
        return imgRect
    }

    override fun drawItem(
        canvas: Canvas,
        rect: Rect,
        product: Product,
        image: Bitmap?,
        priceText: String
    ) {
        val (r, imgRect) = layout(rect)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FAFAFA") }
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#BBBBBB")
        }
        canvas.drawRect(r, bg)
        canvas.drawRect(r, border)

        val headerH = 48
        val headerRect = Rect(r.left, r.top, r.right, r.top + headerH)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#111111") }
        canvas.drawRect(headerRect, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(
            product.displayName.take(24),
            (r.left + 14).toFloat(),
            (r.top + 32).toFloat(),
            titlePaint
        )

        image?.let { canvas.drawBitmap(it, null, imgRect, null) }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#222222")
            textSize = 14f
        }
        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }

        val textLeft = imgRect.right + 12
        canvas.drawText(
            (product.description ?: "").take(110),
            textLeft.toFloat(),
            (imgRect.top + 18).toFloat(),
            bodyPaint
        )
        canvas.drawText(
            priceText,
            textLeft.toFloat(),
            (r.bottom - 18).toFloat(),
            pricePaint
        )
    }

    private fun layout(rect: Rect): Pair<Rect, Rect> {
        val pad = 14
        val r = Rect(rect.left + pad, rect.top + pad, rect.right - pad, rect.bottom - pad)

        val headerH = 48
        val imgRect = Rect(
            r.left + 14,
            r.top + headerH + 14,
            r.left + 190,                // ancho fijo estilo “ficha”
            r.top + headerH + 190
        )

        return r to imgRect
    }
}
