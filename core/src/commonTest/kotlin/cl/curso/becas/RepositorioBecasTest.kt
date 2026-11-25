package cl.curso.becas

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Pruebas del almacén en memoria (IDs relativos, porque el Singleton es compartido). */
class RepositorioBecasTest {

    @Test
    fun guardarAsignaElSiguienteIdYAgregaAlFinal() {
        val antes = RepositorioBecas.obtenerTodas()

        val nueva = RepositorioBecas.guardar("Prueba Repositorio", 9.0, 1000.0, "Aprobada", "Ok")

        val despues = RepositorioBecas.obtenerTodas()
        assertEquals(antes.size + 1, despues.size)
        assertEquals(nueva, despues.last())
        if (antes.isNotEmpty()) assertEquals(antes.last().id + 1, nueva.id)
    }

    @Test
    fun obtenerTodasEntregaUnaCopia() {
        RepositorioBecas.guardar("Copia Uno", 9.0, 1000.0, "Aprobada", "Ok")
        val copia = RepositorioBecas.obtenerTodas()

        RepositorioBecas.guardar("Copia Dos", 9.0, 1000.0, "Aprobada", "Ok")

        // La copia obtenida antes no cambia cuando el repositorio recibe datos nuevos.
        assertTrue(RepositorioBecas.obtenerTodas().size > copia.size)
    }
}
