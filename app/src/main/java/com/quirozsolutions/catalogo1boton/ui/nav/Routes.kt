package com.quirozsolutions.catalogo1boton.ui.nav

object Routes {
    const val PRODUCTS = "products"

    // Ruta base para abrir formulario nuevo
    const val PRODUCT_FORM = "product_form"
    // Ruta patrón para NavHost (con argumento)
    const val PRODUCT_FORM_ROUTE = "product_form?productId={productId}"

    const val GENERATE = "generate"
    const val SETTINGS = "settings"
    const val RESTORE = "restore"

    fun productForm(productId: String? = null): String =
        if (productId.isNullOrBlank()) PRODUCT_FORM
        else "product_form?productId=$productId"
}
