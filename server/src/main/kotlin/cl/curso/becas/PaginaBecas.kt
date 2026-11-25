package cl.curso.becas

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.html.respondHtml
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.HTML
import kotlinx.html.InputType
import kotlinx.html.TBODY
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr
import kotlinx.html.unsafe

/**
 * Lo que se muestra en el formulario: los textos tal como los escribió el usuario
 * y, si los hay, los errores de validación de cada campo.
 */
data class FormularioWeb(
    val nombre: String = "",
    val promedio: String = "",
    val ingresos: String = "",
    val errores: ErroresValidacion? = null,
)

/** Columnas de la tabla del historial, en el orden de la guía. */
private val COLUMNAS_HISTORIAL = listOf("ID", "Estudiante", "Promedio", "Ingresos", "Estado", "Observación")

/** Responde con la página completa, consultando el historial en [RepositorioBecas]. */
suspend fun mostrarPagina(
    call: ApplicationCall,
    formulario: FormularioWeb,
    estado: HttpStatusCode = HttpStatusCode.OK,
) {
    val historial = RepositorioBecas.obtenerTodas()
    call.respondHtml(estado) { paginaBecas(formulario, historial) }
}

/**
 * Estructura de la página: formulario arriba e historial abajo.
 *
 * kotlinx.html escapa automáticamente los textos (`+texto`), así que un nombre con
 * `<script>` se muestra como texto y no se ejecuta.
 */
fun HTML.paginaBecas(formulario: FormularioWeb, historial: List<SolicitudBeca>) {
    attributes["lang"] = "es"
    head {
        meta(charset = "utf-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1")
        title { +"Sistema de Becas - Kotlin Web (Ktor)" }
        style { unsafe { +ESTILOS } }
    }
    body {
        div(classes = "contenedor") {
            div(classes = "tarjeta") {
                h1 { +"Postulación a Beca Estudiantil" }
                formularioSolicitud(formulario)
            }
            div(classes = "tarjeta") {
                h2 { +"Historial de Solicitudes Guardadas" }
                tablaHistorial(historial)
            }
        }
    }
}

/** Formulario: nombre a ancho completo; promedio e ingresos lado a lado. */
private fun FlowContent.formularioSolicitud(formulario: FormularioWeb) {
    val errores = formulario.errores
    form(action = "/", method = FormMethod.post) {
        campo("nombre", "Nombre del Estudiante", formulario.nombre, errores?.nombre.orEmpty())
        div(classes = "fila") {
            campo("promedio", "Promedio (1.0 - 10.0)", formulario.promedio, errores?.promedio.orEmpty(), decimal = true)
            campo("ingresos", "Ingresos Familiares ($)", formulario.ingresos, errores?.ingresos.orEmpty(), decimal = true)
        }
        button(type = ButtonType.submit, classes = "boton") { +"Evaluar y Guardar Solicitud" }
    }
}

/**
 * Un campo del formulario con su etiqueta y sus mensajes de error en rojo debajo.
 *
 * Se usa `type="text"` (no `number`) para que el usuario pueda escribir coma decimal;
 * la conversión y la validación real las hace el servidor con [ValidadorSolicitud].
 * `required` es solo una ayuda del navegador.
 */
private fun FlowContent.campo(
    nombreCampo: String,
    etiqueta: String,
    valor: String,
    errores: List<String>,
    decimal: Boolean = false,
) {
    div(classes = "campo") {
        label { htmlFor = nombreCampo; +etiqueta }
        input(type = InputType.text, name = nombreCampo) {
            id = nombreCampo
            value = valor
            required = true
            if (decimal) attributes["inputmode"] = "decimal" // teclado numérico en móviles
            if (errores.isNotEmpty()) {
                classes = setOf("invalido")
                attributes["aria-invalid"] = "true"
            }
        }
        for (error in errores) {
            p(classes = "error") { +error }
        }
    }
}

/** Tabla del historial, o un aviso si todavía no hay solicitudes. */
private fun FlowContent.tablaHistorial(historial: List<SolicitudBeca>) {
    if (historial.isEmpty()) {
        p(classes = "vacio") { +"Aún no hay solicitudes guardadas." }
        return
    }
    div(classes = "tabla-contenedor") {
        table {
            thead {
                tr { for (columna in COLUMNAS_HISTORIAL) th { +columna } }
            }
            tbody {
                for (solicitud in historial) filaSolicitud(solicitud)
            }
        }
    }
}

/** Una fila del historial. El estado lleva una clase CSS para pintarlo verde o rojo. */
private fun TBODY.filaSolicitud(solicitud: SolicitudBeca) {
    tr {
        td { +solicitud.id.toString() }
        td { +solicitud.nombreEstudiante }
        td { +solicitud.promedio.toString() }
        td { +formatearIngresos(solicitud.ingresosFamiliares) }
        // "Aprobada" → clase "estado-aprobada"; "Rechazada" → "estado-rechazada".
        td(classes = "estado-${solicitud.estado.lowercase()}") { +solicitud.estado }
        td { +solicitud.motivo }
    }
}
