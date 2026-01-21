package com.quirozsolutions.catalogo1boton.infra.pdf.templates

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import com.quirozsolutions.catalogo1boton.domain.model.Product

interface PdfTemplate {

    fun columns(): Int

    fun itemsPerPage(): Int

    /** 🔧 ESTE MÉTODO ES CLAVE */
    fun imageMaxSidePx(): Int

    fun drawItem(
        canvas: Canvas,
        rect: Rect,
        product: Product,
        image: Bitmap?,
        priceText: String
    )
}
