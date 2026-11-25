package cl.curso.becas

/**
 * Servicio que aplica las reglas de negocio de las becas.
 *
 * Es el único lugar del sistema donde se decide si una solicitud se aprueba o se rechaza.
 * La interfaz (Web o Escritorio) solo le entrega los datos ya validados.
 */
object EvaluadorBecas {

    /** RN-01: promedio mínimo exigido (inclusive). */
    private const val promedioMinimo = 8.5

    /** RN-02: ingresos familiares máximos permitidos (inclusive). */
    private const val ingresosMaximos = 1500.0

    private const val ESTADO_APROBADA = "Aprobada"
    private const val ESTADO_RECHAZADA = "Rechazada"
    private const val MOTIVO_APROBADA = "Cumple con todos los requisitos."
    private const val MOTIVO_PROMEDIO = "Promedio debe ser >= 8.5."
    private const val MOTIVO_INGRESOS = "Ingresos superan máximo de $1500."

    /**
     * Evalúa una solicitud y la guarda en [RepositorioBecas], sea aprobada o rechazada (RN-04).
     *
     * Si falla más de un requisito, el motivo incluye **todos** los incumplimientos.
     *
     * @return la solicitud guardada, con su ID, estado y motivo.
     */
    fun procesarSolicitud(nombre: String, prom: Double, ing: Double): SolicitudBeca {
        val motivosRechazo = buscarMotivosRechazo(prom, ing)

        // RN-03: se aprueba solo si no hay ningún motivo de rechazo.
        val estado = if (motivosRechazo.isEmpty()) ESTADO_APROBADA else ESTADO_RECHAZADA
        val motivo = if (motivosRechazo.isEmpty()) MOTIVO_APROBADA else motivosRechazo.joinToString(" ")

        return RepositorioBecas.guardar(nombre, prom, ing, estado, motivo)
    }

    /** Revisa cada regla por separado y junta los motivos de las que no se cumplen. */
    private fun buscarMotivosRechazo(prom: Double, ing: Double): List<String> {
        val motivos = mutableListOf<String>()
        if (!cumpleRequisitoAcademico(prom)) motivos.add(MOTIVO_PROMEDIO)
        if (!cumpleRequisitoSocioeconomico(ing)) motivos.add(MOTIVO_INGRESOS)
        return motivos
    }

    /** RN-01: el promedio debe ser mayor o igual al mínimo. */
    private fun cumpleRequisitoAcademico(prom: Double): Boolean = prom >= promedioMinimo

    /** RN-02: los ingresos deben ser menores o iguales al máximo. */
    private fun cumpleRequisitoSocioeconomico(ing: Double): Boolean = ing <= ingresosMaximos
}
