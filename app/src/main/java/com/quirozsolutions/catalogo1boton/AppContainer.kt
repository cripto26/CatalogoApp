package com.quirozsolutions.catalogo1boton

import android.content.Context
import com.quirozsolutions.catalogo1boton.data.db.AppDatabase
import com.quirozsolutions.catalogo1boton.data.repo.ProductRepository
import com.quirozsolutions.catalogo1boton.infra.backup.BackupManager
import com.quirozsolutions.catalogo1boton.infra.backup.RestoreManager
import com.quirozsolutions.catalogo1boton.infra.drive.DriveSyncManager
import com.quirozsolutions.catalogo1boton.infra.drive.GoogleAuthManager
import com.quirozsolutions.catalogo1boton.infra.files.ImageStore
import com.quirozsolutions.catalogo1boton.infra.pdf.PdfCatalogGenerator
import com.quirozsolutions.catalogo1boton.infra.work.WorkScheduler
import com.quirozsolutions.catalogo1boton.ui.state.AppState

class AppContainer(context: Context) {

    private val db = AppDatabase.build(context)

    val productRepository = ProductRepository(db.productDao())
    val imageStore = ImageStore(context)

    val pdfGenerator = PdfCatalogGenerator(context, imageStore)

    val backupManager = BackupManager(context, imageStore, productRepository)
    val restoreManager = RestoreManager(context, imageStore, productRepository)

    val authManager = GoogleAuthManager(context)
    val driveSyncManager = DriveSyncManager(context, authManager)

    val workScheduler = WorkScheduler(context)

    // ✅ Nuevo: preferencias (cliente, redes del vendedor, logo)
    val appState = AppState(context, this)
}
