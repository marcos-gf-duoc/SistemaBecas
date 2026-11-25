package cl.curso.becas

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Pruebas del estado de la pantalla de Escritorio, sin abrir ninguna ventana.
 * [RepositorioBecas] es compartido, así que se comparan tamaños relativos.
 */
class EstadoPantallaBecasTest {

    @Test
    fun registroValidoActualizaHistorialYLimpiaFormulario() {
        val estado = EstadoPantallaBecas()
        val cantidadAntes = estado.historial.size

        estado.nombre = "Ana Sofía Martínez"
        estado.promedio = "9,2"
        estado.ingresos = "1200"
        estado.evaluarYGuardar()

        assertEquals(cantidadAntes + 1, estado.historial.size)
        assertEquals("Aprobada", estado.historial.last().estado)
        assertEquals("", estado.nombre)
        assertEquals("", estado.promedio)
        assertEquals("", estado.ingresos)
        assertNull(estado.errores)
    }

    @Test
    fun datosInvalidosMuestranErroresYConservanLoEscrito() {
        val estado = EstadoPantallaBecas()
        val cantidadAntes = RepositorioBecas.obtenerTodas().size

        estado.nombre = "Al"
        estado.promedio = "11"
        estado.ingresos = "-5"
        estado.evaluarYGuardar()

        val errores = assertNotNull(estado.errores)
        assertEquals(listOf("El nombre debe tener al menos 3 caracteres."), errores.nombre)
        assertEquals(listOf("El promedio debe estar entre 1.0 y 10.0."), errores.promedio)
        assertEquals(listOf("Los ingresos no pueden ser negativos."), errores.ingresos)
        assertEquals("Al", estado.nombre)
        assertEquals("11", estado.promedio)
        assertEquals("-5", estado.ingresos)
        assertEquals(cantidadAntes, RepositorioBecas.obtenerTodas().size)
    }

    @Test
    fun corregirLosDatosBorraLosErrores() {
        val estado = EstadoPantallaBecas()
        estado.nombre = ""
        estado.promedio = "abc"
        estado.ingresos = "100"
        estado.evaluarYGuardar()
        assertNotNull(estado.errores)

        estado.nombre = "Carlos Mendoza"
        estado.promedio = "7.8"
        estado.evaluarYGuardar()

        assertNull(estado.errores)
        assertEquals("Rechazada", estado.historial.last().estado)
    }

    @Test
    fun colorDelEstadoEsVerdeORojo() {
        assertEquals(VerdeAprobada, colorDeEstado("Aprobada"))
        assertEquals(RojoRechazada, colorDeEstado("Rechazada"))
    }
}
