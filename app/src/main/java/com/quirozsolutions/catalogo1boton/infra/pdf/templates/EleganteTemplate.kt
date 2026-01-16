package com.quirozsolutions.catalogo1boton.infra.pdf.templates

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.quirozsolutions.catalogo1boton.domain.model.Product
import com.quirozsolutions.catalogo1boton.domain.model.displayName

class EleganteTemplate : PdfTemplate {

    override fun columns() = 2
    override fun itemsPerPage() = 6

    override fun drawItem(
        canvas: Canvas,
        rect: Rect,
        product: Product,
        image: Bitmap?,
        priceText: String
    ) {
        val pad = 14
        val r = Rect(rect.left + pad, rect.top + pad, rect.right - pad, rect.bottom - pad)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FAFAFA")
            style = Paint.Style.FILL
        }
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#BBBBBB")
        }

        // Fondo + borde
        canvas.drawRect(r, bg)
        canvas.drawRect(r, border)

        // Header
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

        // Imagen
        val imgRect = Rect(r.left + 14, r.top + headerH + 14, r.left + 190, r.top + headerH + 190)
        image?.let { canvas.drawBitmap(it, null, imgRect, null) }

        // Texto derecha
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
        val desc = (product.description ?: "").take(110)

        // Descripción
        canvas.drawText(
            desc,
            textLeft.toFloat(),
            (imgRect.top + 18).toFloat(),
            bodyPaint
        )

        // Precio abajo a la derecha
        canvas.drawText(
            priceText,
            textLeft.toFloat(),
            (r.bottom - 18).toFloat(),
            pricePaint
        )
    }
}
