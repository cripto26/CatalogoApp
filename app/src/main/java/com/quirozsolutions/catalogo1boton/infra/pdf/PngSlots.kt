package com.quirozsolutions.catalogo1boton.infra.pdf

import android.graphics.PointF
import android.graphics.Rect

data class PngSlot(
    val imageRect: Rect,
    val pricePoint: PointF
)

object PngSlots {

    /**
     * Slots para: templates/tamplate_minimalista_minimalista.png (1024x761)
     * - 3 columnas x 2 filas
     * - imageRect = marco negro
     * - pricePoint = centrado abajo del marco (dentro del recuadro)
     */
    fun minimalista6(): List<PngSlot> {
        // Marcos de imagen detectados (aprox) en tu PNG:
        // (x,y,w,h): (76,34,243,254), (391,34,242,254), (706,34,242,254)
        //           (78,404,243,254), (391,404,242,254), (705,404,242,254)

        // Rects “outer” (marco total de cada tarjeta) para ubicar el precio (aprox)
        // (x,y,w,h): (63,15,274,349), (374,15,275,349), (688,15,275,349)
        //           (63,384,274,349), (374,384,275,349), (688,384,275,349)

        val padInsideFrame = 6 // para no tapar el borde negro del marco

        fun slot(
            cardLeft: Int, cardTop: Int, cardW: Int, cardH: Int,
            frameLeft: Int, frameTop: Int, frameW: Int, frameH: Int
        ): PngSlot {
            val image = Rect(
                frameLeft + padInsideFrame,
                frameTop + padInsideFrame,
                frameLeft + frameW - padInsideFrame,
                frameTop + frameH - padInsideFrame
            )
            val cardCenterX = (cardLeft + cardW / 2f)
            val priceBaselineY = (cardTop + cardH - 24f) // baseline dentro de la tarjeta
            return PngSlot(imageRect = image, pricePoint = PointF(cardCenterX, priceBaselineY))
        }

        return listOf(
            slot(63, 15, 274, 349, 76, 34, 243, 254),
            slot(374, 15, 275, 349, 391, 34, 242, 254),
            slot(688, 15, 275, 349, 706, 34, 242, 254),
            slot(63, 384, 274, 349, 78, 404, 243, 254),
            slot(374, 384, 275, 349, 391, 404, 242, 254),
            slot(688, 384, 275, 349, 705, 404, 242, 254),
        )
    }
}
