package cl.curso.becas

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

/** Puerto en el que escucha el servidor web: http://localhost:8080/ */
const val PUERTO = 8080

/** Punto de entrada de la versión Web: arranca Ktor con el motor Netty. */
fun main() {
    embeddedServer(Netty, port = PUERTO, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/**
 * Configura las rutas de la aplicación.
 *
 * - `GET /`  → muestra el formulario vacío y el historial.
 * - `POST /` → recibe el formulario, lo valida, lo evalúa y lo guarda.
 */
fun Application.module() {
    routing {
        get("/") {
            mostrarPagina(call, FormularioWeb())
        }
        post("/") {
            procesarFormulario(call)
        }
    }
}

/**
 * Atiende el envío del formulario.
 *
 * La capa web no aplica reglas de negocio: valida con [ValidadorSolicitud] (núcleo),
 * evalúa con [EvaluadorBecas] (núcleo) y solo decide qué mostrar.
 */
private suspend fun procesarFormulario(call: ApplicationCall) {
    val parametros = call.receiveParameters()
    val nombre = parametros["nombre"].orEmpty()
    val promedio = parametros["promedio"].orEmpty()
    val ingresos = parametros["ingresos"].orEmpty()

    when (val resultado = ValidadorSolicitud.validar(nombre, promedio, ingresos)) {
        is ResultadoValidacion.Valida -> {
            EvaluadorBecas.procesarSolicitud(resultado.nombre, resultado.promedio, resultado.ingresos)
            // Patrón Post/Redirect/Get: al recargar la página no se reenvía el formulario.
            call.respondRedirect("/")
        }
        is ResultadoValidacion.Invalida -> {
            // No se evalúa ni se guarda. Se vuelve a mostrar la página con los errores
            // y con lo que el usuario escribió, para que no tenga que tipearlo de nuevo.
            val formulario = FormularioWeb(nombre, promedio, ingresos, resultado.errores)
            mostrarPagina(call, formulario, HttpStatusCode.BadRequest)
        }
    }
}
