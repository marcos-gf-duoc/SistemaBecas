package cl.curso.becas

/**
 * Una solicitud de beca ya evaluada.
 *
 * Es una `data class` inmutable (solo `val`): una vez evaluada y guardada, la solicitud
 * no cambia. Así el historial es confiable (RN-04, trazabilidad).
 *
 * @property id identificador correlativo asignado por [RepositorioBecas].
 * @property nombreEstudiante nombre del postulante.
 * @property promedio promedio de notas (escala 1.0 - 10.0).
 * @property ingresosFamiliares ingresos familiares en dólares.
 * @property estado `"Aprobada"` o `"Rechazada"`.
 * @property motivo explicación del resultado de la evaluación.
 */
data class SolicitudBeca(
    val id: Int,
    val nombreEstudiante: String,
    val promedio: Double,
    val ingresosFamiliares: Double,
    val estado: String,
    val motivo: String,
)
