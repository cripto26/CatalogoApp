package com.quirozsolutions.catalogo1boton.domain.model


/**
 * Nombre “visible” del producto para PDF/UI.
 * Como tu Product no tiene "name", usamos description (si existe) o el id.
 */
val Product.displayName: String
    get() {
        val d = (description ?: "").trim()
        return when {
            d.isNotEmpty() -> d
            id.isNotBlank() -> id
            else -> "Producto"
        }
    }
