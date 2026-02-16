package com.quirozsolutions.catalogo1boton.infra.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.quirozsolutions.catalogo1boton.domain.model.CatalogTemplate
import com.quirozsolutions.catalogo1boton.domain.model.Product
import com.quirozsolutions.catalogo1boton.domain.model.displayName
import com.quirozsolutions.catalogo1boton.infra.files.ImageStore
import com.quirozsolutions.catalogo1boton.infra.pdf.templates.TemplateRegistry
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class PdfCatalogGenerator(
    private val context: Context,
    private val imageStore: ImageStore
) {

    /**
     * Mantengo tu método anterior para no romper llamadas existentes.
     */
    fun generateMinimalistaCatalog(
        products: List<Product>,
        storeLogoPath: String?,
        sellerWhatsapp: String,
        sellerInstagram: String,
        debugDrawSlots: Boolean = false,
        onProgress: (cur: Int, total: Int) -> Unit = { _, _ -> }
    ): File {
        return generateCatalog(
            products = products,
            storeLogoPath = storeLogoPath,
            sellerWhatsapp = sellerWhatsapp,
            sellerInstagram = sellerInstagram,
            template = CatalogTemplate.MINIMALISTA,
            debugDrawSlots = debugDrawSlots,
            onProgress = onProgress
        )
    }

    /**
     * Genera el PDF.
     * - Si template == MINIMALISTA y existe assets/templates/tamplate.png -> usa modo PNG (1024x761) tipo “polaroid”.
     * - Si no, usa el modo A4 actual con TemplateRegistry.
     */
    fun generateCatalog(
        products: List<Product>,
        storeLogoPath: String?,
        sellerWhatsapp: String,
        sellerInstagram: String,
        template: CatalogTemplate,
        debugDrawSlots: Boolean = false,
        onProgress: (cur: Int, total: Int) -> Unit = { _, _ -> }
    ): File {

        // ¿Existe tu plantilla PNG?
        val templateBmp = if (template == CatalogTemplate.MINIMALISTA) {
            loadAssetBitmap("templates/tamplate.png")
        } else null

        // Si está la PNG y es MINIMALISTA -> volvemos al modo “bueno” (como tu version anterior)
        val usePngMinimalista = (template == CatalogTemplate.MINIMALISTA && templateBmp != null)

        // Tamaño página:
        // - Modo PNG: 1024 x 761 (igual a tu plantilla)
        // - Modo A4: 595 x 842
        val pageW = if (usePngMinimalista) 1024 else 595
        val pageH = if (usePngMinimalista) 761 else 842

        val doc = PdfDocument()

        // ---- Progreso total (pasos fijos + 1 por producto) ----
        val totalSteps = (products.size.coerceAtLeast(0)) + 3
        var curStep = 0

        fun bump() {
            curStep += 1
            onProgress(curStep.coerceAtMost(totalSteps), totalSteps)
        }

        onProgress(0, totalSteps)
        bump()

        // ---- Paints portada ----
        val paintTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintSub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 12f
        }

        // ---- Portada ----
        run {
            val portadaBmp = loadAssetBitmap("templates/portada.png")

            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 1).create()
            val page = doc.startPage(pageInfo)
            val c = page.canvas

            c.drawColor(Color.WHITE)

            if (usePngMinimalista && portadaBmp != null) {
                // Portada diseñada en PNG
                c.drawBitmap(portadaBmp, null, Rect(0, 0, pageW, pageH), null)
            } else {
                // Portada programática (tu versión actual)
                val margin = 36
                val centerX = pageW / 2

                val maxLogoW = pageW - margin * 2
                val maxLogoH = 180
                val logoDst = Rect(margin, 70, margin + maxLogoW, 70 + maxLogoH)

                val logoBmp = storeLogoPath?.let {
                    val maxSidePx = pointsToPx(max(logoDst.width(), logoDst.height()), COVER_LOGO_DPI)
                    imageStore.loadBitmapForPdf(it, maxSidePx)
                }
                if (logoBmp != null) {
                    val dst = fitRect(
                        srcW = logoBmp.width,
                        srcH = logoBmp.height,
                        dstLeft = logoDst.left,
                        dstTop = logoDst.top,
                        dstW = logoDst.width(),
                        dstH = logoDst.height()
                    )
                    c.drawBitmap(logoBmp, null, dst, null)
                    logoBmp.recycle()
                }

                val title = "Catálogo"
                val titleW = paintTitle.measureText(title)
                c.drawText(title, centerX - (titleW / 2f), 320f, paintTitle)

                var y = 380f
                if (sellerInstagram.isNotBlank()) {
                    c.drawText("Instagram: $sellerInstagram", margin.toFloat(), y, paintSub)
                    y += 22f
                }
                if (sellerWhatsapp.isNotBlank()) {
                    c.drawText("WhatsApp: $sellerWhatsapp", margin.toFloat(), y, paintSub)
                    y += 22f
                }

                c.drawText("Generado desde la app", margin.toFloat(), (pageH - 40).toFloat(), paintSub)
            }

            doc.finishPage(page)
            portadaBmp?.recycle()
        }

        bump()

        val priceFmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        var pageNumber = 2
        var index = 0

        if (usePngMinimalista) {
            // =========================
            // MODO PNG (polaroid 6 slots)
            // =========================
            val slots = PngSlots.minimalista6()

            val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#3E3E3E")
                textSize = 38f
                typeface = Typeface.DEFAULT_BOLD
            }

            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#3E3E3E")
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
            }

            // -------------------------
            // AJUSTES DE ESPACIADO
            // (para que NOMBRE y PRECIO no queden pegados)
            // -------------------------
            val PRICE_Y_OFFSET = 14f     // baja un poquito el precio (más aire arriba)
            val NAME_TO_PRICE_GAP = 18f  // espacio mínimo entre nombre y precio
            val NAME_TOP_PADDING = 16    // separa el nombre de la foto

            while (index < products.size) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber).create()
                val page = doc.startPage(pageInfo)
                val c = page.canvas

                c.drawColor(Color.WHITE)

                // Fondo plantilla
                c.drawBitmap(templateBmp!!, null, Rect(0, 0, pageW, pageH), null)

                val maxOnThisPage = min(slots.size, products.size - index)

                for (i in 0 until maxOnThisPage) {
                    val product = products[index + i]
                    val slot = slots[i]

                    val priceText = priceFmt.format(product.priceCents / 100.0)

                    // Foto (centerCrop dentro del marco negro)
                    val bmp = product.imagePath?.takeIf { it.isNotBlank() }?.let { path ->
                        val maxSidePx = pointsToPx(
                            max(slot.imageRect.width(), slot.imageRect.height()),
                            PRODUCT_IMAGE_DPI
                        )
                        imageStore.loadBitmapForPdf(path, maxSidePx)
                    }

                    if (bmp != null) {
                        drawBitmapCenterCrop(c, bmp, slot.imageRect)
                        bmp.recycle()
                    }

                    // Precio centrado (un poquito más abajo)
                    val tw = pricePaint.measureText(priceText)
                    val priceY = slot.pricePoint.y + PRICE_Y_OFFSET
                    c.drawText(
                        priceText,
                        slot.pricePoint.x - (tw / 2f),
                        priceY,
                        pricePaint
                    )

                    // ==============================
                    // NOMBRE: solo si hay descripción
                    // (displayName ya NO devuelve UUID)
                    // ==============================
                    val name = product.displayName.trim()
                    if (name.isNotBlank() && name != "Producto") {

                        // Área del nombre: empieza un poco más abajo de la foto
                        // y termina con un "gap" antes del precio (para que no quede pegado)
                        val titleAreaTop = slot.imageRect.bottom + NAME_TOP_PADDING
                        val titleAreaBottom = (priceY - NAME_TO_PRICE_GAP).toInt()

                        if (titleAreaBottom > titleAreaTop) {
                            val titleArea = Rect(
                                slot.imageRect.left,
                                titleAreaTop,
                                slot.imageRect.right,
                                titleAreaBottom
                            )

                            drawWrappedCenteredText(
                                canvas = c,
                                text = name,
                                area = titleArea,
                                paint = namePaint,
                                maxLines = 2
                            )
                        }
                    }

                    bump()
                }

                doc.finishPage(page)
                pageNumber += 1
                index += maxOnThisPage
            }

            templateBmp?.recycle()

        } else {
            // =========================
            // MODO A4 (tu versión actual)
            // =========================
            val tpl = TemplateRegistry.resolve(template)

            val cols = tpl.columns().coerceAtLeast(1)
            val perPage = tpl.itemsPerPage().coerceAtLeast(1)
            val rows = ceil(perPage / cols.toDouble()).toInt().coerceAtLeast(1)

            val marginX = 28
            val marginTop = 36
            val marginBottom = 28
            val gap = 14

            val usableW = pageW - marginX * 2 - gap * (cols - 1)
            val usableH = pageH - marginTop - marginBottom - gap * (rows - 1)

            val cellW = usableW / cols
            val cellH = usableH / rows

            while (index < products.size) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber).create()
                val page = doc.startPage(pageInfo)
                val c = page.canvas

                c.drawColor(Color.WHITE)

                if (debugDrawSlots) {
                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        color = Color.LTGRAY
                        strokeWidth = 1f
                    }
                    for (r in 0 until rows) {
                        for (col in 0 until cols) {
                            val left = marginX + col * (cellW + gap)
                            val top = marginTop + r * (cellH + gap)
                            val rect = Rect(left, top, left + cellW, top + cellH)
                            c.drawRect(rect, p)
                        }
                    }
                }

                val maxOnThisPage = min(perPage, products.size - index)

                for (i in 0 until maxOnThisPage) {
                    val product = products[index + i]

                    val row = i / cols
                    val col = i % cols

                    val left = marginX + col * (cellW + gap)
                    val top = marginTop + row * (cellH + gap)
                    val cellRect = Rect(left, top, left + cellW, top + cellH)

                    val priceText = priceFmt.format(product.priceCents / 100.0)

                    val bmp = product.imagePath?.takeIf { it.isNotBlank() }?.let { path ->
                        val imgRect = tpl.imageRect(cellRect)
                        val maxSidePx = pointsToPx(max(imgRect.width(), imgRect.height()), PRODUCT_IMAGE_DPI)
                        imageStore.loadBitmapForPdf(path, maxSidePx)
                    }

                    tpl.drawItem(
                        canvas = c,
                        rect = cellRect,
                        product = product,
                        image = bmp,
                        priceText = priceText
                    )

                    bmp?.recycle()
                    bump()
                }

                doc.finishPage(page)
                pageNumber += 1
                index += maxOnThisPage
            }
        }

        // ---- Guardar PDF ----
        val outFile = File(context.cacheDir, "catalogo_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outFile).use { fos -> doc.writeTo(fos) }
        doc.close()

        bump()
        return outFile
    }

    // =========================
    // Helpers
    // =========================

    private fun loadAssetBitmap(assetPath: String): Bitmap? {
        return try {
            context.assets.open(assetPath).use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * CenterCrop sin deformar dentro de dst.
     */
    private fun drawBitmapCenterCrop(canvas: Canvas, bmp: Bitmap, dst: Rect) {
        val srcW = bmp.width.toFloat()
        val srcH = bmp.height.toFloat()
        val dstW = dst.width().toFloat()
        val dstH = dst.height().toFloat()

        val scale = max(dstW / srcW, dstH / srcH)
        val scaledW = srcW * scale
        val scaledH = srcH * scale

        val left = dst.left + (dstW - scaledW) / 2f
        val top = dst.top + (dstH - scaledH) / 2f

        val m = Matrix().apply {
            postScale(scale, scale)
            postTranslate(left, top)
        }

        canvas.save()
        canvas.clipRect(dst)
        canvas.drawBitmap(bmp, m, null)
        canvas.restore()
    }

    private fun drawWrappedCenteredText(
        canvas: Canvas,
        text: String,
        area: Rect,
        paint: Paint,
        maxLines: Int
    ) {
        if (area.width() <= 0 || area.height() <= 0) return
        val clean = text.trim()
        if (clean.isEmpty()) return

        val words = clean.split(Regex("\\s+"))
        val lines = ArrayList<String>(maxLines)

        var i = 0
        while (i < words.size && lines.size < maxLines) {
            var line = words[i]
            i++

            while (i < words.size) {
                val candidate = "$line ${words[i]}"
                if (paint.measureText(candidate) <= area.width()) {
                    line = candidate
                    i++
                } else break
            }
            lines.add(line)
        }

        // Si sobró texto, elipsiza última línea
        val hasMore = i < words.size
        if (hasMore && lines.isNotEmpty()) {
            lines[lines.lastIndex] = ellipsizeToWidth(lines.last(), area.width().toFloat(), paint)
        }

        val lineH = paint.fontSpacing
        var y = area.top - paint.ascent() // baseline

        for (line in lines) {
            if (y > area.bottom) break
            val w = paint.measureText(line)
            val x = area.exactCenterX() - (w / 2f)
            canvas.drawText(line, x, y, paint)
            y += lineH
        }
    }

    private fun ellipsizeToWidth(text: String, maxW: Float, paint: Paint): String {
        val ell = "…"
        if (paint.measureText(text) <= maxW) return text
        if (paint.measureText(ell) > maxW) return ""

        var end = text.length
        while (end > 0) {
            val candidate = text.substring(0, end).trimEnd() + ell
            if (paint.measureText(candidate) <= maxW) return candidate
            end--
        }
        return ell
    }

    /**
     * Convierte puntos (72dpi) a píxeles objetivo (DPI elegido).
     */
    private fun pointsToPx(points: Int, dpi: Int): Int {
        return ((points.toFloat() * dpi.toFloat()) / 72f).toInt().coerceAtLeast(1)
    }

    /**
     * Calcula un rect destino manteniendo aspect ratio (contain).
     */
    private fun fitRect(
        srcW: Int,
        srcH: Int,
        dstLeft: Int,
        dstTop: Int,
        dstW: Int,
        dstH: Int
    ): Rect {
        if (srcW <= 0 || srcH <= 0) return Rect(dstLeft, dstTop, dstLeft + dstW, dstTop + dstH)

        val srcRatio = srcW.toFloat() / srcH.toFloat()
        val dstRatio = dstW.toFloat() / dstH.toFloat()

        val w: Int
        val h: Int

        if (srcRatio > dstRatio) {
            w = dstW
            h = (dstW / srcRatio).toInt()
        } else {
            h = dstH
            w = (dstH * srcRatio).toInt()
        }

        val left = dstLeft + (dstW - w) / 2
        val top = dstTop + (dstH - h) / 2
        return Rect(left, top, left + w, top + h)
    }

    companion object {
        private const val PRODUCT_IMAGE_DPI = 160
        private const val COVER_LOGO_DPI = 200
    }
}
