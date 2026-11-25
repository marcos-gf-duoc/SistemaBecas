package cl.curso.becas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Formulario de postulación: nombre a ancho completo; promedio e ingresos lado a lado.
 *
 * Solo muestra y modifica el [estado]; la validación y la evaluación ocurren en
 * [EstadoPantallaBecas.evaluarYGuardar], que delega en el núcleo.
 */
@Composable
fun FormularioSolicitud(estado: EstadoPantallaBecas) {
    val errores = estado.errores
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoTexto(
            valor = estado.nombre,
            alCambiar = { estado.nombre = it },
            etiqueta = "Nombre del Estudiante",
            errores = errores?.nombre.orEmpty(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top, // si un campo muestra error, el otro no se mueve
        ) {
            CampoTexto(
                valor = estado.promedio,
                alCambiar = { estado.promedio = it },
                etiqueta = "Promedio (1.0 - 10.0)",
                errores = errores?.promedio.orEmpty(),
                esNumero = true,
                modifier = Modifier.weight(1f),
            )
            CampoTexto(
                valor = estado.ingresos,
                alCambiar = { estado.ingresos = it },
                etiqueta = "Ingresos Familiares ($)",
                errores = errores?.ingresos.orEmpty(),
                esNumero = true,
                modifier = Modifier.weight(1f),
            )
        }
        Button(
            onClick = { estado.evaluarYGuardar() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("Evaluar y Guardar Solicitud")
        }
    }
}

/**
 * Un `OutlinedTextField` que se marca en rojo (`isError`) y muestra debajo
 * todos los mensajes de error de su campo.
 */
@Composable
private fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    errores: List<String>,
    modifier: Modifier = Modifier,
    esNumero: Boolean = false,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        isError = errores.isNotEmpty(),
        supportingText = if (errores.isEmpty()) null else {
            { Column { for (error in errores) Text(error) } }
        },
        singleLine = true,
        // Teclado decimal: ayuda en pantallas táctiles; igual se aceptan "8,9" y "8.9".
        keyboardOptions = if (esNumero) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
        modifier = modifier,
    )
}
