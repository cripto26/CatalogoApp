package com.quirozsolutions.catalogo1boton.infra.pdf.templates

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import com.quirozsolutions.catalogo1boton.domain.model.Product

interface PdfTemplate {

    fun columns(): Int

    fun itemsPerPage(): Int

    /**
     * Rect exacto donde la plantilla dibuja la imagen (en coordenadas del PDF page/canvas).
     * Esto permite cargar la imagen con el tamaño JUSTO y que el PDF quede liviano.
     */
    fun imageRect(itemRect: Rect): Rect

    fun drawItem(
        canvas: Canvas,
        rect: Rect,
        product: Product,
        image: Bitmap?,
        priceText: String
    )
}
