package cl.curso.becas

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/** Punto de entrada de la versión Escritorio: solo abre la ventana con la UI de `app/shared`. */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sistema de Becas - Kotlin Desktop",
        state = rememberWindowState(width = 1000.dp, height = 760.dp),
    ) {
        App()
    }
}
