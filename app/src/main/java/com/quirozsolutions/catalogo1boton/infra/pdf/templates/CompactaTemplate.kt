package com.quirozsolutions.catalogo1boton.infra.pdf.templates

import android.graphics.*
import com.quirozsolutions.catalogo1boton.domain.model.Product
import com.quirozsolutions.catalogo1boton.domain.model.displayName

class CompactaTemplate : PdfTemplate {

    override fun columns() = 3
    override fun itemsPerPage() = 9

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

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = Color.parseColor("#CCCCCC")
        }
        canvas.drawRect(r, border)

        image?.let { canvas.drawBitmap(it, null, imgRect, null) }

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
        }
        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            textSize = 12f
        }

        val name = product.displayName.take(18)
        canvas.drawText(name, (r.left + 8).toFloat(), (imgRect.bottom + 18).toFloat(), namePaint)
        canvas.drawText(priceText, (r.left + 8).toFloat(), (r.bottom - 10).toFloat(), pricePaint)
    }

    private fun layout(rect: Rect): Pair<Rect, Rect> {
        val pad = 10
        val r = Rect(rect.left + pad, rect.top + pad, rect.right - pad, rect.bottom - pad)

        val imgBottom = r.top + (r.height() * 0.62f).toInt()
        val imgRect = Rect(r.left + 8, r.top + 8, r.right - 8, imgBottom)

        return r to imgRect
    }
}
