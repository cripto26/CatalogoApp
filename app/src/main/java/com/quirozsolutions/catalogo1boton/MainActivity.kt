package com.quirozsolutions.catalogo1boton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.quirozsolutions.catalogo1boton.ui.nav.AppNav
import com.quirozsolutions.catalogo1boton.ui.theme.Catalogo1botonTheme

class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        container = AppContainer(applicationContext)

        setContent {
            Catalogo1botonTheme {
                AppNav(container = container)
            }
        }
    }
}
