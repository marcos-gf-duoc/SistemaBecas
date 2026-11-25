package cl.curso.becas

/**
 * Resultado de validar el formulario: o los datos son válidos, o hay errores.
 *
 * Es un `sealed interface`: solo existen estos dos casos, así que un `when` sobre él
 * obliga a la interfaz a manejar ambos.
 */
sealed interface ResultadoValidacion {

    /** Todos los campos son válidos; trae los valores ya convertidos. */
    data class Valida(
        val nombre: String,
        val promedio: Double,
        val ingresos: Double,
    ) : ResultadoValidacion

    /** Algún campo es inválido; la solicitud no debe evaluarse ni guardarse. */
    data class Invalida(val errores: ErroresValidacion) : ResultadoValidacion
}

/**
 * Errores de validación agrupados por campo.
 *
 * Separarlos por campo permite al Escritorio marcar cada `OutlinedTextField` con su
 * propio mensaje; la Web puede mostrar la lista completa con [todos].
 */
data class ErroresValidacion(
    val nombre: List<String>,
    val promedio: List<String>,
    val ingresos: List<String>,
) {
    /** Todos los errores juntos, en el orden del formulario. */
    val todos: List<String>
        get() = nombre + promedio + ingresos

    /** `true` si no hay ningún error en ningún campo. */
    fun estaVacio(): Boolean = todos.isEmpty()
}

/**
 * Valida los datos del formulario **antes** de llamar a [EvaluadorBecas.procesarSolicitud].
 *
 * Recibe los valores como `String`, tal como llegan del formulario, y revisa todos los
 * campos a la vez para mostrarle al usuario todos los errores de una sola vez.
 * Vive en el núcleo para que Web y Escritorio usen exactamente las mismas reglas.
 */
object ValidadorSolicitud {

    private const val LARGO_MINIMO_NOMBRE = 3
    private const val PROMEDIO_MINIMO_ESCALA = 1.0
    private const val PROMEDIO_MAXIMO_ESCALA = 10.0

    /**
     * Valida los tres campos del formulario.
     *
     * @return [ResultadoValidacion.Valida] con los datos convertidos, o
     *   [ResultadoValidacion.Invalida] con la lista completa de errores.
     */
    fun validar(nombre: String, promedio: String, ingresos: String): ResultadoValidacion {
        val nombreLimpio = nombre.trim()
        val promedioNumero = convertirNumero(promedio)
        val ingresosNumero = convertirNumero(ingresos)

        val errores = ErroresValidacion(
            nombre = validarNombre(nombreLimpio),
            promedio = validarPromedio(promedioNumero),
            ingresos = validarIngresos(ingresosNumero),
        )

        // Si no hay errores, los números no son null (los validadores lo revisaron).
        return if (errores.estaVacio() && promedioNumero != null && ingresosNumero != null) {
            ResultadoValidacion.Valida(nombreLimpio, promedioNumero, ingresosNumero)
        } else {
            ResultadoValidacion.Invalida(errores)
        }
    }

    /**
     * Convierte un texto a número aceptando coma decimal (`"8,9"` → `8.9`).
     *
     * Devuelve `null` si no es un número. También descarta `"NaN"` e `"Infinity"`,
     * que `toDoubleOrNull()` aceptaría pero no son valores reales para el formulario.
     */
    private fun convertirNumero(texto: String): Double? {
        val numero = texto.trim().replace(',', '.').toDoubleOrNull()
        return if (numero != null && numero.isFinite()) numero else null
    }

    /** Nombre: obligatorio, mínimo 3 caracteres, solo letras (con tildes, ñ, ü) y espacios. */
    private fun validarNombre(nombre: String): List<String> {
        // Si está vacío no tiene sentido informar también el largo mínimo.
        if (nombre.isEmpty()) return listOf("El nombre es obligatorio.")

        val errores = mutableListOf<String>()
        if (nombre.length < LARGO_MINIMO_NOMBRE) {
            errores.add("El nombre debe tener al menos 3 caracteres.")
        }
        if (!nombre.all { it.isLetter() || it == ' ' }) {
            errores.add("El nombre solo puede contener letras y espacios.")
        }
        return errores
    }

    /** Promedio: debe ser numérico y estar entre 1.0 y 10.0 (inclusive). */
    private fun validarPromedio(promedio: Double?): List<String> = when {
        promedio == null -> listOf("El promedio debe ser un número.")
        promedio !in PROMEDIO_MINIMO_ESCALA..PROMEDIO_MAXIMO_ESCALA ->
            listOf("El promedio debe estar entre 1.0 y 10.0.")
        else -> emptyList()
    }

    /** Ingresos: deben ser numéricos y no negativos. */
    private fun validarIngresos(ingresos: Double?): List<String> = when {
        ingresos == null -> listOf("Los ingresos deben ser un número.")
        ingresos < 0.0 -> listOf("Los ingresos no pueden ser negativos.")
        else -> emptyList()
    }
}
