# Sistema de Evaluación y Registro de Becas Estudiantiles

Proyecto pedagógico del curso de programación en Kotlin. Muestra cómo separar
**lógica de negocio**, **almacenamiento** e **interfaz**, y cómo reutilizar un mismo núcleo
en dos plataformas: **Web (Ktor + kotlinx.html)** y **Escritorio (Compose Multiplatform Desktop)**.

Los datos viven en memoria y se pierden al cerrar la aplicación (es intencional: no hay base
de datos ni archivos).

## Estructura

| Módulo | Contenido |
|---|---|
| [`core/`](./core/src/commonMain/kotlin/cl/curso/becas) | Núcleo compartido: `SolicitudBeca`, `RepositorioBecas`, `EvaluadorBecas`, `ValidadorSolicitud` y `formatearIngresos`. No depende de Ktor ni de Compose. |
| [`server/`](./server/src/main/kotlin/cl/curso/becas) | Aplicación Web con Ktor y kotlinx.html (puerto 8080). |
| [`app/shared/`](./app/shared/src/commonMain/kotlin/cl/curso/becas) | Interfaz Compose: estado de la pantalla, formulario y tabla del historial. |
| [`app/desktopApp/`](./app/desktopApp/src/main/kotlin/cl/curso/becas) | Punto de entrada de escritorio: solo abre la ventana. |

Flujo de dependencias:

```
Presentación (Web / Escritorio)
   ├── valida con ──► ValidadorSolicitud
   ├── invoca ──────► EvaluadorBecas ── guarda en ──► RepositorioBecas ── almacena ──► SolicitudBeca
   └── consulta ────► RepositorioBecas (historial)
```

La presentación nunca aplica reglas de negocio ni escribe directamente en el repositorio.

## Reglas de negocio

| Código | Regla | Condición |
|---|---|---|
| RN-01 | Requisito académico | Promedio `>= 8.5` (escala 1.0 – 10.0) |
| RN-02 | Requisito socioeconómico | Ingresos familiares `<= $1500.00` |
| RN-03 | Aprobación integral | Cumple RN-01 **y** RN-02 → `Aprobada`; si no → `Rechazada` |
| RN-04 | Trazabilidad | Toda solicitud evaluada (aprobada o rechazada) se guarda con ID correlativo |

Si fallan ambos requisitos, el motivo informa los dos. Los límites son inclusivos
(8.5 y 1500 se aprueban).

## Validación de entradas

Si algún campo es inválido, la solicitud **no se evalúa ni se guarda** y se muestran todos los
errores a la vez, conservando lo escrito. La validación vive en el núcleo, así que Web y
Escritorio aplican exactamente las mismas reglas:

- **Nombre:** obligatorio, mínimo 3 caracteres, solo letras (incluye tildes, ñ, ü) y espacios.
- **Promedio:** numérico, entre 1.0 y 10.0.
- **Ingresos:** numéricos y no negativos.
- Se acepta coma decimal (`8,9` → `8.9`).

## Requisitos

- JDK 21.
- No hace falta instalar Gradle: se usa el *wrapper* incluido (`gradlew` / `gradlew.bat`).

## Cómo ejecutar

En Windows usa `.\gradlew.bat` (o `.\gradlew`); en macOS/Linux, `./gradlew`.

| Qué | Comando |
|---|---|
| Versión Web | `./gradlew :server:run` → abrir <http://localhost:8080/> |
| Versión Escritorio | `./gradlew :app:desktopApp:run` |
| Escritorio con recarga en caliente | `./gradlew :app:desktopApp:hotRun --auto` |

## Pruebas

| Qué | Comando |
|---|---|
| Núcleo (reglas, validación, repositorio) | `./gradlew :core:allTests` |
| Web (rutas, Post/Redirect/Get, errores) | `./gradlew :server:test` |
| Escritorio (estado de la pantalla) | `./gradlew :app:shared:allTests` |
| Todo junto | `./gradlew :core:allTests :server:test :app:shared:allTests` |

## Casos de prueba de referencia

| Nombre | Promedio | Ingresos | Resultado |
|---|---|---|---|
| Ana Sofía Martínez | 9.2 | 1200 | Aprobada |
| Carlos Mendoza | 7.8 | 1100 | Rechazada — promedio |
| Lucía Fernández | 9.0 | 1850 | Rechazada — ingresos |
| Diego Morales | 8.9 | 1400 | Aprobada |
| Valeria Castro | 8.1 | 950 | Rechazada — promedio |
| Javier Rojas | 9.5 | 2100 | Rechazada — ingresos |
| (inventado) | 7.0 | 3000 | Rechazada — ambos motivos |
| Límite | 8.5 | 1500 | Aprobada |
