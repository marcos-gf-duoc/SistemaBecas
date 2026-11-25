package cl.curso.becas

import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Pruebas de la versión Web. Como [RepositorioBecas] es un Singleton compartido,
 * se comparan tamaños relativos (antes / después).
 */
class ApplicationTest {

    /** Cliente que NO sigue redirecciones, para poder comprobar el Post/Redirect/Get. */
    private fun ApplicationTestBuilder.clienteSinRedirecciones() =
        createClient { followRedirects = false }

    @Test
    fun getMuestraFormularioEHistorial() = testApplication {
        application { module() }

        val respuesta = client.get("/")

        assertEquals(HttpStatusCode.OK, respuesta.status)
        val html = respuesta.bodyAsText()
        assertContains(html, "<title>Sistema de Becas - Kotlin Web (Ktor)</title>")
        assertContains(html, "Postulación a Beca Estudiantil")
        assertContains(html, "Evaluar y Guardar Solicitud")
        assertContains(html, "Historial de Solicitudes Guardadas")
    }

    @Test
    fun postValidoGuardaYRedirigeAlInicio() = testApplication {
        application { module() }
        val cantidadAntes = RepositorioBecas.obtenerTodas().size

        val respuesta = clienteSinRedirecciones().submitForm(
            url = "/",
            formParameters = parameters {
                append("nombre", "Diego Morales")
                append("promedio", "8,9") // coma decimal
                append("ingresos", "1400")
            },
        )

        assertEquals(HttpStatusCode.Found, respuesta.status)
        assertEquals("/", respuesta.headers[HttpHeaders.Location])
        val todas = RepositorioBecas.obtenerTodas()
        assertEquals(cantidadAntes + 1, todas.size)
        assertEquals("Aprobada", todas.last().estado)
        assertEquals(8.9, todas.last().promedio)
    }

    @Test
    fun trasGuardarElHistorialMuestraLaSolicitud() = testApplication {
        application { module() }

        client.submitForm(
            url = "/",
            formParameters = parameters {
                append("nombre", "Lucía Fernández")
                append("promedio", "9.0")
                append("ingresos", "1850")
            },
        )
        // El cliente de pruebas no sigue redirecciones de un POST (el navegador sí),
        // así que hacemos nosotros el GET al que redirige el servidor.
        val respuesta = client.get("/")

        assertEquals(HttpStatusCode.OK, respuesta.status)
        val html = respuesta.bodyAsText()
        assertContains(html, "Lucía Fernández")
        assertContains(html, "\$1850.0")
        assertContains(html, "estado-rechazada")
        assertContains(html, "Ingresos superan máximo de \$1500.")
    }

    @Test
    fun postInvalidoNoGuardaYMuestraErroresConLosValores() = testApplication {
        application { module() }
        val cantidadAntes = RepositorioBecas.obtenerTodas().size

        val respuesta = client.submitForm(
            url = "/",
            formParameters = parameters {
                append("nombre", "")
                append("promedio", "abc")
                append("ingresos", "-5")
            },
        )

        assertEquals(HttpStatusCode.BadRequest, respuesta.status)
        val html = respuesta.bodyAsText()
        assertContains(html, "El nombre es obligatorio.")
        assertContains(html, "El promedio debe ser un número.")
        assertContains(html, "Los ingresos no pueden ser negativos.")
        assertContains(html, "value=\"abc\"") // se conserva lo que escribió el usuario
        assertContains(html, "value=\"-5\"")
        assertEquals(cantidadAntes, RepositorioBecas.obtenerTodas().size)
    }

    @Test
    fun losTextosSeEscapanEnElHtml() = testApplication {
        application { module() }

        val respuesta = client.submitForm(
            url = "/",
            formParameters = parameters {
                append("nombre", "<script>")
                append("promedio", "9")
                append("ingresos", "100")
            },
        )

        // Es inválido (tiene símbolos) y se re-muestra escapado, nunca como etiqueta real.
        val html = respuesta.bodyAsText()
        assertContains(html, "&lt;script&gt;")
    }
}
