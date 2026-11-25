package cl.curso.becas

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pruebas de las reglas de negocio RN-01 a RN-04.
 *
 * Como [RepositorioBecas] es un Singleton compartido por todas las pruebas, nunca se
 * asume un ID absoluto: se comparan tamaños e IDs relativos (antes / después).
 */
class EvaluadorBecasTest {

    private val motivoAprobada = "Cumple con todos los requisitos."
    private val motivoPromedio = "Promedio debe ser >= 8.5."
    private val motivoIngresos = "Ingresos superan máximo de $1500."

    /** Ayudante: evalúa y verifica estado y motivo exactos. */
    private fun verificar(nombre: String, prom: Double, ing: Double, estado: String, motivo: String) {
        val solicitud = EvaluadorBecas.procesarSolicitud(nombre, prom, ing)
        assertEquals(estado, solicitud.estado, "Estado de $nombre")
        assertEquals(motivo, solicitud.motivo, "Motivo de $nombre")
    }

    // --- Casos de la tabla de referencia ---

    @Test
    fun anaSofiaMartinezEsAprobada() =
        verificar("Ana Sofía Martínez", 9.2, 1200.0, "Aprobada", motivoAprobada)

    @Test
    fun carlosMendozaEsRechazadoPorPromedio() =
        verificar("Carlos Mendoza", 7.8, 1100.0, "Rechazada", motivoPromedio)

    @Test
    fun luciaFernandezEsRechazadaPorIngresos() =
        verificar("Lucía Fernández", 9.0, 1850.0, "Rechazada", motivoIngresos)

    @Test
    fun diegoMoralesEsAprobado() =
        verificar("Diego Morales", 8.9, 1400.0, "Aprobada", motivoAprobada)

    @Test
    fun valeriaCastroEsRechazadaPorPromedio() =
        verificar("Valeria Castro", 8.1, 950.0, "Rechazada", motivoPromedio)

    @Test
    fun javierRojasEsRechazadoPorIngresos() =
        verificar("Javier Rojas", 9.5, 2100.0, "Rechazada", motivoIngresos)

    @Test
    fun fallarAmbosCriteriosInformaLosDosMotivos() =
        verificar("Pedro Inventado", 7.0, 3000.0, "Rechazada", "$motivoPromedio $motivoIngresos")

    // --- Valores límite (son inclusivos) ---

    @Test
    fun limitesExactosSonAprobados() =
        verificar("Caso Límite", 8.5, 1500.0, "Aprobada", motivoAprobada)

    @Test
    fun promedioJustoBajoElLimiteEsRechazado() =
        verificar("Bajo Límite", 8.49, 1500.0, "Rechazada", motivoPromedio)

    @Test
    fun ingresosJustoSobreElLimiteSonRechazados() =
        verificar("Sobre Límite", 8.5, 1500.01, "Rechazada", motivoIngresos)

    // --- RN-04: trazabilidad ---

    @Test
    fun lasSolicitudesRechazadasTambienSeGuardan() {
        val cantidadAntes = RepositorioBecas.obtenerTodas().size

        val solicitud = EvaluadorBecas.procesarSolicitud("Rechazo Guardado", 5.0, 5000.0)

        val todas = RepositorioBecas.obtenerTodas()
        assertEquals(cantidadAntes + 1, todas.size)
        assertEquals(solicitud, todas.last())
    }

    @Test
    fun losIdsSonCorrelativos() {
        val primera = EvaluadorBecas.procesarSolicitud("Primera Persona", 9.0, 1000.0)
        val segunda = EvaluadorBecas.procesarSolicitud("Segunda Persona", 7.0, 1000.0)

        assertEquals(primera.id + 1, segunda.id)
    }

    @Test
    fun seGuardanLosDatosRecibidos() {
        val solicitud = EvaluadorBecas.procesarSolicitud("Datos Exactos", 9.1, 1234.5)

        assertEquals("Datos Exactos", solicitud.nombreEstudiante)
        assertEquals(9.1, solicitud.promedio)
        assertEquals(1234.5, solicitud.ingresosFamiliares)
    }
}
