package cl.curso.becas

/**
 * Almacén en memoria de las solicitudes evaluadas.
 *
 * Es un `object` (Singleton): existe una sola instancia en toda la aplicación, de modo que
 * todas las pantallas ven el mismo historial. Los datos se pierden al cerrar la app
 * (no hay persistencia real, es intencional).
 */
object RepositorioBecas {

    /** Lista interna; es privada para que nadie de afuera la modifique directamente. */
    private val baseDeDatos: MutableList<SolicitudBeca> = mutableListOf()

    /** Último ID entregado. Empieza en 0, así la primera solicitud recibe el ID 1. */
    private var contadorId: Int = 0

    /**
     * Crea una [SolicitudBeca] con el siguiente ID correlativo y la guarda (RN-04).
     *
     * @return la solicitud recién guardada, con su ID asignado.
     */
    fun guardar(
        nombre: String,
        prom: Double,
        ing: Double,
        est: String,
        mot: String,
    ): SolicitudBeca {
        contadorId++
        val solicitud = SolicitudBeca(
            id = contadorId,
            nombreEstudiante = nombre,
            promedio = prom,
            ingresosFamiliares = ing,
            estado = est,
            motivo = mot,
        )
        baseDeDatos.add(solicitud)
        return solicitud
    }

    /**
     * Devuelve todas las solicitudes guardadas, en orden de registro.
     *
     * Se entrega una **copia** de solo lectura (`toList()`): quien la reciba no puede
     * agregar ni borrar elementos del repositorio.
     */
    fun obtenerTodas(): List<SolicitudBeca> = baseDeDatos.toList()
}
