package cl.curso.becas

/**
 * Formatea los ingresos como en la guía: `1200.0` → `"$1200.0"`.
 *
 * Vive en el núcleo porque Web y Escritorio deben mostrar los montos exactamente igual.
 * Es solo presentación de datos: no aplica ninguna regla de negocio.
 */
fun formatearIngresos(ingresos: Double): String = "\$$ingresos"
