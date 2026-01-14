package com.quirozsolutions.catalogo1boton.domain.model


data class Product(
    val id: String,
    val priceCents: Long,
    val description: String?,
    val imagePath: String,
    val createdAt: Long,
    val updatedAt: Long
)
