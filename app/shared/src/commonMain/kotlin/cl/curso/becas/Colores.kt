package cl.curso.becas

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Violeta principal de la guía (botón y encabezado de la tabla). */
val VioletaPrincipal = Color(0xFF6200EE)

/** Color del estado "Aprobada". */
val VerdeAprobada = Color(0xFF2E7D32)

/** Color del estado "Rechazada" y de los errores de validación. */
val RojoRechazada = Color(0xFFC62828)

/** Fondo gris violáceo suave detrás de las tarjetas. */
val FondoPantalla = Color(0xFFF4F2F8)

/**
 * Paleta de Material 3 con los colores de la guía. Al definir `primary` y `error`,
 * los `Button` salen violetas y los `OutlinedTextField` con `isError` salen rojos.
 */
val EsquemaColoresBecas = lightColorScheme(
    primary = VioletaPrincipal,
    error = RojoRechazada,
    background = FondoPantalla,
)

/** Verde para "Aprobada", rojo para cualquier otro estado ("Rechazada"). */
fun colorDeEstado(estado: String): Color =
    if (estado == "Aprobada") VerdeAprobada else RojoRechazada
