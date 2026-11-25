package cl.curso.becas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Estado de la pantalla de becas: lo que el usuario escribe, los errores y el historial.
 *
 * Cada propiedad usa `mutableStateOf`: cuando cambia, Compose vuelve a dibujar
 * automáticamente las partes de la pantalla que la leen (por eso la tabla se actualiza
 * al instante). Es una clase simple, sin ViewModel ni librerías extra, y no aplica reglas
 * de negocio: delega en [ValidadorSolicitud] y [EvaluadorBecas] (núcleo).
 */
class EstadoPantallaBecas {

    /** Texto del campo "Nombre del Estudiante", tal como lo escribe el usuario. */
    var nombre by mutableStateOf("")

    /** Texto del campo "Promedio" (se guarda como texto para admitir `"8,9"`). */
    var promedio by mutableStateOf("")

    /** Texto del campo "Ingresos Familiares". */
    var ingresos by mutableStateOf("")

    /** Errores del último intento, o `null` si no hay. Solo esta clase los cambia. */
    var errores by mutableStateOf<ErroresValidacion?>(null)
        private set

    /** Copia del historial guardado en [RepositorioBecas]. */
    var historial by mutableStateOf(RepositorioBecas.obtenerTodas())
        private set

    /**
     * Acción del botón "Evaluar y Guardar Solicitud".
     *
     * - Si hay errores: los muestra y conserva lo escrito (no se evalúa ni se guarda).
     * - Si todo es válido: evalúa, guarda, refresca el historial y limpia el formulario.
     */
    fun evaluarYGuardar() {
        when (val resultado = ValidadorSolicitud.validar(nombre, promedio, ingresos)) {
            is ResultadoValidacion.Invalida -> errores = resultado.errores
            is ResultadoValidacion.Valida -> {
                EvaluadorBecas.procesarSolicitud(resultado.nombre, resultado.promedio, resultado.ingresos)
                historial = RepositorioBecas.obtenerTodas()
                limpiarFormulario()
            }
        }
    }

    /** Deja los campos vacíos y sin errores, listos para la siguiente solicitud. */
    private fun limpiarFormulario() {
        nombre = ""
        promedio = ""
        ingresos = ""
        errores = null
    }
}
