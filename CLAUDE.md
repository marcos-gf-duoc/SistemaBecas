# CLAUDE.md — Sistema de Evaluación y Registro de Becas Estudiantiles

## Contexto
Proyecto pedagógico de un curso de programación en Kotlin. El objetivo es enseñar a separar
**lógica de negocio**, **almacenamiento** e **interfaz**, y a reutilizar un mismo núcleo
en dos plataformas: **Web (Ktor + kotlinx.html)** y **Escritorio (Compose Multiplatform Desktop)**.

Este es un proyecto de aprendizaje: prioriza código claro, legible y bien comentado
por sobre soluciones sofisticadas.

## Restricciones (no negociables)
- **Sin Android, iOS ni Web/Wasm.** Solo los targets Desktop (JVM) y Server (Ktor).
  Android corresponde a fases más avanzadas del curso.
- **Sin persistencia real.** Nada de bases de datos, archivos ni ORM. Los datos viven en memoria
  (`MutableList` dentro de un `object`) y se pierden al cerrar la app. Es intencional.
- **Sin librerías extra** más allá de las que exige el stack (Ktor, kotlinx.html, Compose).
  No agregar inyección de dependencias, ViewModels de terceros, serialización, etc.
- **No modificar la estructura de clases del UML** (nombres, propiedades y métodos) salvo lo
  indicado en "Decisiones adicionales".

## Estructura del proyecto (Kotlin Multiplatform, Gradle)
- `core/` → **núcleo compartido** (`src/commonMain`): modelo, repositorio, evaluador y validación.
  Es lo que usan tanto el escritorio como el servidor.
  **Ninguna clase de este módulo debe depender de Ktor ni de Compose.**
- `app/shared/` → interfaz Compose (pantallas y composables). Depende de `core`.
- `app/desktopApp/` → punto de entrada de escritorio (`main` + `Window`). Solo arranca la UI de `app/shared`.
- `server/` → aplicación web (Ktor + HTML DSL), puerto **8080**. Depende de `core`.

Paquete base: `cl.curso.becas`.

Comandos habituales (en Windows usar `gradlew.bat` o `.\gradlew`):
- Escritorio: `./gradlew :app:desktopApp:run`
- Escritorio con recarga en caliente: `./gradlew :app:desktopApp:hotRun --auto`
- Web: `./gradlew :server:run` → abrir `http://localhost:8080/`
- Pruebas del núcleo: `./gradlew :core:allTests` (o `:core:jvmTest`)
- Eliminar el código de ejemplo de la plantilla (Greeting, etc.) al implementar, sin romper la compilación.

## Núcleo compartido (según el diagrama UML)

### `SolicitudBeca` — `data class` (inmutable, solo `val`)
| Propiedad | Tipo |
|---|---|
| `id` | `Int` |
| `nombreEstudiante` | `String` |
| `promedio` | `Double` |
| `ingresosFamiliares` | `Double` |
| `estado` | `String` |
| `motivo` | `String` |

### `RepositorioBecas` — `object` (Singleton)
- `private val baseDeDatos: MutableList<SolicitudBeca>`
- `private var contadorId: Int` (IDs correlativos desde 1)
- `fun guardar(nombre, prom, ing, est, mot): SolicitudBeca` → asigna el siguiente ID y guarda.
- `fun obtenerTodas(): List<SolicitudBeca>` → devuelve una **copia de solo lectura**
  (no exponer la lista mutable).

### `EvaluadorBecas` — `object` (servicio)
- `private const val promedioMinimo = 8.5`
- `private const val ingresosMaximos = 1500.0`
- `fun procesarSolicitud(nombre, prom, ing)` → aplica las reglas de negocio y guarda el
  resultado en `RepositorioBecas`.

### Flujo de dependencias
Presentación → **invoca** → `EvaluadorBecas` → **guarda en** → `RepositorioBecas` → **almacena** → `SolicitudBeca`
Presentación → **consulta** → `RepositorioBecas` (para mostrar el historial)

La capa de presentación **nunca** aplica reglas de negocio ni escribe directamente en el repositorio.

## Decisiones adicionales
Acordadas con el docente al iniciar la etapa 1:
- `EvaluadorBecas.procesarSolicitud` **devuelve la `SolicitudBeca` guardada** (el UML no fija
  el tipo de retorno; nombre y parámetros no cambian). Facilita las pruebas.
- La validación vive en un `object ValidadorSolicitud` del núcleo. Devuelve un
  `sealed interface ResultadoValidacion` con dos casos: `Valida` (datos convertidos, nombre con
  `trim()`) e `Invalida` (errores agrupados **por campo** en `ErroresValidacion`, más la
  propiedad `todos` con la lista completa).
- Si el nombre está vacío solo se informa `El nombre es obligatorio.` (no también el mínimo de
  caracteres). Las letras se verifican con `Char.isLetter()`.
- Los textos `"NaN"` e `"Infinity"` (que `toDoubleOrNull()` acepta) se rechazan como
  "no es un número" usando `isFinite()`.
- Las pruebas no agregan métodos al repositorio (p. ej. `limpiar()`): como es un Singleton
  compartido, verifican IDs y tamaños **relativos** (antes/después).
- El código de ejemplo de la plantilla (`sayHello`, `Greeting`, `Platform`, recursos de
  ejemplo y las dependencias de ViewModel/lifecycle) ya fue eliminado (etapas 2 y 3).
- `formatearIngresos` (`1200.0` → `"$1200.0"`) vive en el núcleo (`Formato.kt`) porque la
  usan Web y Escritorio; es solo formato, no regla de negocio.
- Web: un envío inválido responde **400** con la página y los errores; uno válido redirige
  con **302** a `GET /`. La dependencia `ktor-server-html-builder` aporta kotlinx.html.
- Escritorio: el estado de la pantalla está en una clase simple `EstadoPantallaBecas`
  (propiedades `mutableStateOf`, sin ViewModel) con `evaluarYGuardar()`. Así la lógica de la
  pantalla se prueba sin abrir ventanas.

## Reglas de negocio
| Código | Regla | Condición | Resultado |
|---|---|---|---|
| RN-01 | Requisito académico | `promedio >= 8.5` (escala 1.0–10.0) | Si no cumple → `"Rechazada"` |
| RN-02 | Requisito socioeconómico | `ingresosFamiliares <= 1500.00` | Si no cumple → `"Rechazada"` |
| RN-03 | Aprobación integral | Cumple RN-01 **y** RN-02 | `"Aprobada"` |
| RN-04 | Trazabilidad | Toda solicitud **evaluada** se guarda | ID correlativo + guardado en repositorio |

Las solicitudes rechazadas **también se guardan**.

### Textos exactos de estado y motivo
| Caso | `estado` | `motivo` |
|---|---|---|
| Cumple todo | `Aprobada` | `Cumple con todos los requisitos.` |
| Falla solo RN-01 | `Rechazada` | `Promedio debe ser >= 8.5.` |
| Falla solo RN-02 | `Rechazada` | `Ingresos superan máximo de $1500.` |
| Falla RN-01 y RN-02 | `Rechazada` | `Promedio debe ser >= 8.5. Ingresos superan máximo de $1500.` |

**Decisión del docente:** si fallan ambos criterios, se informan **los dos** motivos (no solo el primero).

## Validación de entradas
Decisión: **si algún campo es inválido, la solicitud NO se evalúa ni se guarda** (no consume ID).
Se muestran los errores al usuario y se conservan los valores que escribió.

La validación vive en el **núcleo compartido** para que Web y Escritorio usen exactamente las
mismas reglas. Debe recibir los valores como `String` (tal como llegan del formulario) y devolver
o bien los datos convertidos o bien la **lista completa de errores** (mostrar todos a la vez,
no solo el primero).

| Campo | Regla | Mensaje sugerido |
|---|---|---|
| Nombre | No vacío ni solo espacios (aplicar `trim()`) | `El nombre es obligatorio.` |
| Nombre | Mínimo 3 caracteres | `El nombre debe tener al menos 3 caracteres.` |
| Nombre | Solo letras (incluye tildes, ñ, ü) y espacios | `El nombre solo puede contener letras y espacios.` |
| Promedio | Numérico | `El promedio debe ser un número.` |
| Promedio | Entre 1.0 y 10.0 (inclusive) | `El promedio debe estar entre 1.0 y 10.0.` |
| Ingresos | Numérico | `Los ingresos deben ser un número.` |
| Ingresos | Mayor o igual a 0 | `Los ingresos no pueden ser negativos.` |

- **Aceptar coma decimal** (`"8,9"` → `8.9`): reemplazar `,` por `.` antes de convertir.
  Usar `toDoubleOrNull()`, nunca `toDouble()` sin control.
- Para no alterar la firma del UML, la validación puede implementarse como un `object`
  adicional en el núcleo (p. ej. `ValidadorSolicitud`) que la presentación usa **antes** de
  llamar a `EvaluadorBecas.procesarSolicitud`.

## Interfaz de usuario (debe coincidir con las figuras de la guía)

### Elementos comunes
- Título: **"Postulación a Beca Estudiantil"**.
- Campos: **Nombre del Estudiante**, **Promedio (1.0 - 10.0)**, **Ingresos Familiares ($)**.
- Botón: **"Evaluar y Guardar Solicitud"**.
- Subtítulo: **"Historial de Solicitudes Guardadas"**.
- Tabla con columnas: **ID | Estudiante | Promedio | Ingresos | Estado | Observación**.
- Ingresos con formato `$1200.0`.
- Color principal violeta (≈ `#6200EE`) en botón y encabezado de tabla.
- Estado **"Aprobada" en verde** y **"Rechazada" en rojo**.
- Los errores de validación se muestran cerca del formulario, en rojo.

### Web — Ktor + kotlinx.html
- Servidor en el puerto **8080**.
- `GET /` → renderiza formulario + historial.
- `POST /` (o `/evaluar`) → recibe los parámetros del formulario, valida, evalúa y guarda.
  Tras un envío exitoso, redirigir a `GET /` (patrón Post/Redirect/Get) para evitar
  reenvíos al recargar. Si hay errores, re-renderizar la página con los errores y los
  valores ingresados.
- Título de la pestaña: **"Sistema de Becas - Kotlin Web (Ktor)"**.
- Se pueden usar atributos HTML (`required`, etc.) como ayuda, pero **la validación real
  siempre ocurre en el servidor**.
- Estilos con CSS simple embebido; sin frameworks CSS.

### Escritorio — Compose Multiplatform Desktop
- Título de la ventana: **"Sistema de Becas - Kotlin Desktop"**.
- Layout: nombre a ancho completo; promedio e ingresos lado a lado en una fila.
- Usar `OutlinedTextField`; marcar campos inválidos con `isError` y su mensaje debajo.
- Estado reactivo con `mutableStateOf` / `remember`: la tabla se actualiza al instante
  sin recargar la ventana.
- Tras un registro exitoso, limpiar el formulario.

## Casos de prueba de referencia
| Nombre | Promedio | Ingresos | Resultado esperado |
|---|---|---|---|
| Ana Sofía Martínez | 9.2 | 1200 | Aprobada |
| Carlos Mendoza | 7.8 | 1100 | Rechazada — promedio |
| Lucía Fernández | 9.0 | 1850 | Rechazada — ingresos |
| Diego Morales | 8.9 | 1400 | Aprobada |
| Valeria Castro | 8.1 | 950 | Rechazada — promedio |
| Javier Rojas | 9.5 | 2100 | Rechazada — ingresos |
| (inventado) | 7.0 | 3000 | Rechazada — ambos motivos |
| Límite | 8.5 | 1500 | **Aprobada** (los límites son inclusivos) |
| Inválido | `"abc"` / `11` / `-5` | — | Error de validación, **no se guarda** |
| Coma decimal | `"8,9"` | `"1400"` | Se acepta como 8.9 |

Escribir pruebas unitarias (`kotlin.test`) para el evaluador y el validador cubriendo estos casos,
incluidos los valores límite.

## Convenciones de código
- Idioma: **identificadores, comentarios, mensajes y textos de la UI en español**
  (coherente con el UML: `nombreEstudiante`, `procesarSolicitud`, etc.).
- Comentarios KDoc breves en cada clase y función pública, explicando el *porqué*
  y referenciando la regla aplicada (p. ej. `// RN-01`).
- Funciones cortas y con una sola responsabilidad.
- Preferir `val` sobre `var`; inmutabilidad en el modelo.
- Constantes con nombre, sin "números mágicos" (8.5 y 1500.0 solo en `EvaluadorBecas`).
- Código idiomático pero accesible para estudiantes: evitar abstracciones avanzadas
  innecesarias (genéricos complejos, coroutines si no hacen falta, DSLs propios).

## Forma de trabajar (para Claude Code)
- Avanzar por etapas y confirmar antes de pasar a la siguiente:
  1. Núcleo compartido (modelo, repositorio, evaluador, validador) + pruebas.
  2. Versión Web (Ktor).
  3. Versión Escritorio (Compose).
- Antes de cambios grandes, explicar brevemente el plan.
- Al terminar cada etapa, compilar y ejecutar las pruebas; reportar el resultado.
- Al explicar código, hacerlo en español y con enfoque didáctico (qué hace y por qué).
- Si algo de este documento entra en conflicto con lo que pide el usuario, preguntar.
