package com.quirozsolutions.catalogo1boton.infra.pdf

import android.graphics.Rect

object PngSlots {

    fun ofertas6(): List<Rect> {
        val pageW = 595
        val pageH = 842

        // Ajustados para tu PNG exacto (header alto + footer alto)
        val marginLeft = 36
        val marginRight = 36
        val marginTop = 210
        val marginBottom = 170

        val cols = 3
        val rows = 2

        val gapX = 18
        val gapY = 22

        val availableW = pageW - marginLeft - marginRight
        val availableH = pageH - marginTop - marginBottom

        val cellW = (availableW - gapX * (cols - 1)) / cols
        val cellH = (availableH - gapY * (rows - 1)) / rows

        val slots = mutableListOf<Rect>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = marginLeft + c * (cellW + gapX)
                val top = marginTop + r * (cellH + gapY)
                slots.add(Rect(left, top, left + cellW, top + cellH))
            }
        }
        return slots
    }
}
