package cl.curso.becas

import kotlin.test.Test
import kotlin.test.assertEquals

/** Pruebas del formato de montos que comparten Web y Escritorio. */
class FormatoTest {

    @Test
    fun ingresosSeMuestranConSignoPesoYUnDecimal() {
        assertEquals("\$1200.0", formatearIngresos(1200.0))
        assertEquals("\$1234.5", formatearIngresos(1234.5))
        assertEquals("\$0.0", formatearIngresos(0.0))
    }
}
