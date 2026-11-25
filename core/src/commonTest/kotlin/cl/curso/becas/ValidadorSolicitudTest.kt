package cl.curso.becas

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Pruebas de la validación de entradas del formulario. */
class ValidadorSolicitudTest {

    /** Ayudante: valida y exige que el resultado sea [ResultadoValidacion.Valida]. */
    private fun validarOk(nombre: String, prom: String, ing: String): ResultadoValidacion.Valida =
        assertIs<ResultadoValidacion.Valida>(ValidadorSolicitud.validar(nombre, prom, ing))

    /** Ayudante: valida y devuelve los errores, exigiendo que el resultado sea inválido. */
    private fun erroresDe(nombre: String, prom: String, ing: String): ErroresValidacion =
        assertIs<ResultadoValidacion.Invalida>(ValidadorSolicitud.validar(nombre, prom, ing)).errores

    // --- Datos válidos ---

    @Test
    fun datosValidosSeConviertenCorrectamente() {
        val datos = validarOk("Ana Sofía Martínez", "9.2", "1200")
        assertEquals(ResultadoValidacion.Valida("Ana Sofía Martínez", 9.2, 1200.0), datos)
    }

    @Test
    fun seAceptaComaDecimal() {
        val datos = validarOk("Diego Morales", "8,9", "1400")
        assertEquals(8.9, datos.promedio)
    }

    @Test
    fun elNombreSeEntregaSinEspaciosExtremos() {
        assertEquals("Lucía", validarOk("   Lucía  ", "9", "100").nombre)
    }

    @Test
    fun seAceptanTildesEnieYDieresis() {
        validarOk("Begoña Güemes Núñez", "9", "100")
    }

    @Test
    fun seAceptanLosLimitesDeLaEscalaYCeroIngresos() {
        validarOk("Límite Inferior", "1.0", "0")
        validarOk("Límite Superior", "10.0", "0")
    }

    // --- Nombre ---

    @Test
    fun nombreVacioSoloInformaQueEsObligatorio() {
        assertEquals(listOf("El nombre es obligatorio."), erroresDe("", "9", "100").nombre)
    }

    @Test
    fun nombreConSoloEspaciosEsObligatorio() {
        assertEquals(listOf("El nombre es obligatorio."), erroresDe("    ", "9", "100").nombre)
    }

    @Test
    fun nombreCortoEsRechazado() {
        assertEquals(
            listOf("El nombre debe tener al menos 3 caracteres."),
            erroresDe("Al", "9", "100").nombre,
        )
    }

    @Test
    fun nombreConDigitosOSimbolosEsRechazado() {
        val esperado = listOf("El nombre solo puede contener letras y espacios.")
        assertEquals(esperado, erroresDe("Ana123", "9", "100").nombre)
        assertEquals(esperado, erroresDe("Ana-María", "9", "100").nombre)
    }

    // --- Promedio ---

    @Test
    fun promedioNoNumericoEsRechazado() {
        assertEquals(listOf("El promedio debe ser un número."), erroresDe("Ana Sofía", "abc", "100").promedio)
        assertEquals(listOf("El promedio debe ser un número."), erroresDe("Ana Sofía", "", "100").promedio)
    }

    @Test
    fun promedioFueraDeRangoEsRechazado() {
        val esperado = listOf("El promedio debe estar entre 1.0 y 10.0.")
        assertEquals(esperado, erroresDe("Ana Sofía", "11", "100").promedio)
        assertEquals(esperado, erroresDe("Ana Sofía", "0.9", "100").promedio)
        assertEquals(esperado, erroresDe("Ana Sofía", "10.01", "100").promedio)
    }

    @Test
    fun nanEInfinitoNoSonNumeros() {
        assertEquals(listOf("El promedio debe ser un número."), erroresDe("Ana Sofía", "NaN", "100").promedio)
        assertEquals(listOf("Los ingresos deben ser un número."), erroresDe("Ana Sofía", "9", "Infinity").ingresos)
    }

    // --- Ingresos ---

    @Test
    fun ingresosNoNumericosSonRechazados() {
        assertEquals(listOf("Los ingresos deben ser un número."), erroresDe("Ana Sofía", "9", "mil").ingresos)
    }

    @Test
    fun ingresosNegativosSonRechazados() {
        assertEquals(listOf("Los ingresos no pueden ser negativos."), erroresDe("Ana Sofía", "9", "-5").ingresos)
    }

    // --- Varios errores a la vez ---

    @Test
    fun seInformanTodosLosErroresALaVez() {
        val errores = erroresDe("", "abc", "-5")
        assertEquals(
            listOf(
                "El nombre es obligatorio.",
                "El promedio debe ser un número.",
                "Los ingresos no pueden ser negativos.",
            ),
            errores.todos,
        )
    }

    @Test
    fun soloElCampoInvalidoTieneErrores() {
        val errores = erroresDe("Ana Sofía", "11", "1200")
        assertTrue(errores.nombre.isEmpty())
        assertTrue(errores.ingresos.isEmpty())
        assertEquals(1, errores.promedio.size)
    }
}
