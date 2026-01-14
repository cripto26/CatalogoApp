package com.quirozsolutions.catalogo1boton.ui.nav


object Routes {
    const val PRODUCTS = "products"
    const val PRODUCT_FORM = "product_form?productId={productId}"
    const val GENERATE = "generate"
    const val SETTINGS = "settings"
    const val RESTORE = "restore"

    fun productForm(productId: String? = null) =
        "product_form?productId=${productId ?: ""}"
}
