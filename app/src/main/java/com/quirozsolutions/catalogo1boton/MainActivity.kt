package com.quirozsolutions.catalogo1boton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.quirozsolutions.catalogo1boton.ui.nav.AppNav
import com.quirozsolutions.catalogo1boton.ui.theme.Catalogo1botonTheme

class MainActivity : ComponentActivity() {

    // Container para inyectar dependencias (Room, managers, etc.)
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear el container una sola vez
        container = AppContainer(applicationContext)

        setContent {
            Catalogo1botonTheme {
                AppNav(container = container)
            }
        }
    }
}
