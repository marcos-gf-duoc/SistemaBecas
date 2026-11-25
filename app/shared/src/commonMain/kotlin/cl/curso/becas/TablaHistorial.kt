package cl.curso.becas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Títulos de las columnas, en el orden de la guía. */
private val TITULOS = listOf("ID", "Estudiante", "Promedio", "Ingresos", "Estado", "Observación")

/** Ancho relativo de cada columna (mismo orden que [TITULOS]). */
private val PESOS = listOf(0.5f, 2f, 1f, 1f, 1.2f, 3f)

/**
 * Tabla "Historial de Solicitudes Guardadas".
 *
 * Recibe la lista ya consultada al repositorio; cuando esa lista cambia en
 * [EstadoPantallaBecas], Compose vuelve a dibujar la tabla solo.
 */
@Composable
fun TablaHistorial(historial: List<SolicitudBeca>) {
    if (historial.isEmpty()) {
        Text("Aún no hay solicitudes guardadas.", fontStyle = FontStyle.Italic, color = Color.Gray)
        return
    }
    Column(Modifier.fillMaxWidth()) {
        FilaEncabezado()
        for (solicitud in historial) {
            FilaSolicitud(solicitud)
            HorizontalDivider()
        }
    }
}

/** Fila de títulos con fondo violeta y texto blanco. */
@Composable
private fun FilaEncabezado() {
    Row(Modifier.fillMaxWidth().background(VioletaPrincipal)) {
        TITULOS.forEachIndexed { i, titulo ->
            Celda(titulo, PESOS[i], color = Color.White, negrita = true)
        }
    }
}

/** Una solicitud del historial. El estado se pinta verde o rojo. */
@Composable
private fun FilaSolicitud(solicitud: SolicitudBeca) {
    Row(Modifier.fillMaxWidth()) {
        Celda(solicitud.id.toString(), PESOS[0])
        Celda(solicitud.nombreEstudiante, PESOS[1])
        Celda(solicitud.promedio.toString(), PESOS[2])
        Celda(formatearIngresos(solicitud.ingresosFamiliares), PESOS[3])
        Celda(solicitud.estado, PESOS[4], color = colorDeEstado(solicitud.estado), negrita = true)
        Celda(solicitud.motivo, PESOS[5])
    }
}

/** Celda de texto que ocupa su parte proporcional (`weight`) del ancho de la fila. */
@Composable
private fun RowScope.Celda(
    texto: String,
    peso: Float,
    color: Color = Color.Unspecified,
    negrita: Boolean = false,
) {
    Text(
        text = texto,
        color = color,
        fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.weight(peso).padding(horizontal = 8.dp, vertical = 10.dp),
    )
}
