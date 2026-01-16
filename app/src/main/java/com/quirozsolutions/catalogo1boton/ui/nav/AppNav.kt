package com.quirozsolutions.catalogo1boton.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quirozsolutions.catalogo1boton.AppContainer
import com.quirozsolutions.catalogo1boton.ui.screens.GenerateCatalogScreen
import com.quirozsolutions.catalogo1boton.ui.screens.ProductFormScreen
import com.quirozsolutions.catalogo1boton.ui.screens.ProductListScreen
import com.quirozsolutions.catalogo1boton.ui.screens.RestoreScreen
import com.quirozsolutions.catalogo1boton.ui.screens.SettingsScreen

@Composable
fun AppNav(container: AppContainer) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.PRODUCTS) {

        composable(Routes.PRODUCTS) {
            ProductListScreen(
                container = container,
                onAdd = { nav.navigate(Routes.PRODUCT_FORM) },
                onEdit = { productId ->
                    nav.navigate("${Routes.PRODUCT_FORM}?productId=$productId")
                },
                onGenerate = { nav.navigate(Routes.GENERATE) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onRestore = { nav.navigate(Routes.RESTORE) }
            )
        }

        composable(
            route = Routes.PRODUCT_FORM,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStack ->
            val pid = backStack.arguments?.getString("productId")?.takeIf { it.isNotBlank() }

            ProductFormScreen(
                container = container,
                productId = pid,
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.GENERATE) {
            GenerateCatalogScreen(
                container = container,
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                container = container,
                onBack = { nav.popBackStack() },
                syncNow = { clientName, sharedFolderId ->
                    // 1) Crear backup ZIP local
                    val zip = container.backupManager.buildLatestBackup(clientName)

                    // 2) Subir a Drive (requiere sesión)
                    val account = container.authManager.lastSignedInAccount()
                        ?: throw IllegalStateException("No hay sesión iniciada")

                    // Drive appDataFolder (recomendado)
                    container.driveSyncManager.uploadLatestBackup(
                        account = account,
                        backupZip = zip
                    )

                    // sharedFolderId queda sin uso si usas appDataFolder
                    @Suppress("UNUSED_VARIABLE")
                    val _ignore = sharedFolderId
                }
            )
        }

        composable(Routes.RESTORE) {
            RestoreScreen(
                container = container,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
