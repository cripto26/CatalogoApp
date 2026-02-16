package com.quirozsolutions.catalogo1boton.domain.model

/**
 * Nombre “visible” del producto para PDF/UI.
 * Regla:
 * - Si hay descripción -> úsala.
 * - Si NO hay descripción -> NO mostrar UUID, usar un texto genérico.
 */
val Product.displayName: String
    get() {
        val d = (description ?: "").trim()
        return if (d.isNotEmpty()) d else "Producto"
    }
