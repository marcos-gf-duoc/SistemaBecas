package cl.curso.becas

/**
 * Hoja de estilos embebida en la página (CSS simple, sin frameworks).
 *
 * Colores de la guía: violeta principal `#6200EE` en el botón y el encabezado de la tabla;
 * verde para "Aprobada" y rojo para "Rechazada" y los errores.
 */
val ESTILOS = """
    :root {
        --violeta: #6200EE;
        --violeta-oscuro: #4B00B5;
        --verde: #2E7D32;
        --rojo: #C62828;
        --borde: #C9C5D0;
        --fondo: #F4F2F8;
    }
    * { box-sizing: border-box; }
    body {
        margin: 0;
        font-family: "Segoe UI", Roboto, Arial, sans-serif;
        background: var(--fondo);
        color: #1C1B1F;
    }
    .contenedor { max-width: 960px; margin: 0 auto; padding: 24px 16px; }
    .tarjeta {
        background: #FFFFFF;
        border-radius: 12px;
        padding: 24px;
        margin-bottom: 24px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
    }
    h1 { margin-top: 0; font-size: 1.6rem; }
    h2 { margin-top: 0; font-size: 1.25rem; }

    /* Formulario */
    .fila { display: flex; gap: 16px; flex-wrap: wrap; }
    .fila .campo { flex: 1 1 220px; }
    .campo { margin-bottom: 16px; }
    .campo label { display: block; font-weight: 600; margin-bottom: 6px; }
    .campo input {
        width: 100%;
        padding: 10px 12px;
        font-size: 1rem;
        border: 1px solid var(--borde);
        border-radius: 6px;
    }
    .campo input:focus { outline: 2px solid var(--violeta); border-color: var(--violeta); }
    .campo input.invalido { border: 2px solid var(--rojo); }
    .error { color: var(--rojo); margin: 4px 0 0; font-size: 0.9rem; }
    .boton {
        width: 100%;
        padding: 12px;
        font-size: 1rem;
        font-weight: 600;
        color: #FFFFFF;
        background: var(--violeta);
        border: none;
        border-radius: 6px;
        cursor: pointer;
    }
    .boton:hover { background: var(--violeta-oscuro); }

    /* Historial */
    .tabla-contenedor { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; }
    th { background: var(--violeta); color: #FFFFFF; text-align: left; padding: 10px; }
    td { padding: 10px; border-bottom: 1px solid var(--borde); }
    .estado-aprobada { color: var(--verde); font-weight: 700; }
    .estado-rechazada { color: var(--rojo); font-weight: 700; }
    .vacio { color: #625B71; font-style: italic; }
""".trimIndent()
