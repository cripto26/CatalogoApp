package com.quirozsolutions.catalogo1boton.data.db


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val priceCents: Long,
    val description: String?,
    val imagePath: String,
    val createdAt: Long,
    val updatedAt: Long
)
