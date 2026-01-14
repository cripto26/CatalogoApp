package com.quirozsolutions.catalogo1boton.ui.nav


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.quirozsolutions.catalogo1boton.App
import com.quirozsolutions.catalogo1boton.ui.screens.*

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val container = (ctx.applicationContext as App).container

    NavHost(navController = nav, startDestination = Routes.PRODUCTS) {
        composable(Routes.PRODUCTS) {
            ProductListScreen(
                container = container,
                onAdd = { nav.navigate(Routes.productForm(null)) },
                onEdit = { id -> nav.navigate(Routes.productForm(id)) },
                onGenerate = { nav.navigate(Routes.GENERATE) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onRestore = { nav.navigate(Routes.RESTORE) }
            )
        }
        composable(
            route = Routes.PRODUCT_FORM,
            arguments = listOf(navArgument("productId") { type = NavType.StringType; defaultValue = "" })
        ) { backStack ->
            val pid = backStack.arguments?.getString("productId")?.takeIf { it.isNotBlank() }
            ProductFormScreen(container = container, productId = pid, onBack = { nav.popBackStack() })
        }
        composable(Routes.GENERATE) {
            GenerateCatalogScreen(container = container, onBack = { nav.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(container = container, onBack = { nav.popBackStack() })
        }
        composable(Routes.RESTORE) {
            RestoreScreen(container = container, onBack = { nav.popBackStack() })
        }
    }
}
