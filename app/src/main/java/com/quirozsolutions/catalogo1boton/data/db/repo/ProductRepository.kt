package com.quirozsolutions.catalogo1boton.data.repo


import com.quirozsolutions.catalogo1boton.data.db.ProductDao
import com.quirozsolutions.catalogo1boton.data.db.ProductEntity
import com.quirozsolutions.catalogo1boton.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val dao: ProductDao) {

    fun observeAll(): Flow<List<Product>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getAllOnce(): List<Product> =
        dao.getAllOnce().map { it.toDomain() }

    suspend fun getById(id: String): Product? =
        dao.getById(id)?.toDomain()

    suspend fun upsert(p: Product) {
        dao.upsert(p.toEntity())
    }

    suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    private fun ProductEntity.toDomain() = Product(
        id = id,
        priceCents = priceCents,
        description = description,
        imagePath = imagePath,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Product.toEntity() = ProductEntity(
        id = id,
        priceCents = priceCents,
        description = description,
        imagePath = imagePath,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
