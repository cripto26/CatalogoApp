package com.quirozsolutions.catalogo1boton.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupMetadata(
    val createdAt: Long,
    val clientName: String,
    val deviceName: String,
    val schemaVersion: Int = 1
)

@Serializable
data class BackupProduct(
    val id: String,
    val priceCents: Long,
    val description: String?,
    val imageFileName: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class BackupPayload(
    val metadata: BackupMetadata,
    val products: List<BackupProduct>,
    val sha256: String
)
