<!-- Autor: Samuel Chica Vélez — ID: 000279346 -->
# Guion de sustentación — Eventos e inscripciones

Este guion sirve para dos cosas: **entender el proyecto** y **explicarlo en voz alta** frente al docente. Está escrito en el orden en que conviene presentarlo. Las partes en *cursiva* son frases que puedes decir casi tal cual; el resto es lo que tienes que entender para poder responder preguntas.

> **Antes de sustentar:** lee cada clase en el código mientras lees su sección aquí. Si algo de este guion no coincide con lo que ves, confía en el código. El docente puede preguntar por cualquier línea, así que no repitas de memoria algo que no entiendas.

**Contenido**
1. Presentación en 1 minuto
2. Mapa general: cómo se conectan las clases
3. Demostración paso a paso con los datos de prueba
4. Explicación clase por clase
5. Dónde está cada requisito del enunciado
6. Preguntas probables y cómo responderlas
7. Limitaciones que debes conocer

---

## 1. Presentación en 1 minuto

*"Mi proyecto es el número 11: Eventos e inscripciones. Una organización dicta talleres y conferencias con cupo limitado, y con el papel siempre sobra o falta gente. Mi aplicación permite crear eventos con su agenda, inscribir personas controlando el cupo, mandar a lista de espera cuando el evento se llena, marcar la asistencia por actividad, ver estadísticas y descargar certificados."*

*"Está hecha con Java 21 y Vaadin 25, solo con componentes gratuitos. No uso base de datos: cada tipo de dato vive en su propio archivo de texto, con los campos separados por una barra vertical. Los tres componentes que investigué son ProgressBar para el cupo, DateTimePicker para las fechas y CheckboxGroup para la asistencia. Además hice la bonificación: cuando alguien cancela, el primero de la lista de espera pasa solo a inscrito, y quien asiste al 80 % o más puede descargar su certificado."*

---

## 2. Mapa general

### Capas

```
 VISTAS (lo que ve el usuario)        MODELO (los datos)         ARCHIVOS
 ─────────────────────────────        ──────────────────         ────────
 EventosView, EventoFormView  ───┐
 AgendaView, InscripcionesView   ├──►  ArchivoDatos  ◄──────►   datos/*.txt
 AsistenciaView, EstadisticasView│       (lee y escribe)
 CertificadosView             ───┘            │
        ▲                                     ▼
        │                          Evento, Actividad, Inscripcion, Asistencia
   MainLayout (menú, avisar, confirmar)       (clases con los datos)
```

Regla de oro del diseño: **las vistas nunca tocan los archivos directamente**. Siempre le piden las cosas a `ArchivoDatos`, que es la única clase que sabe leer y escribir los `.txt`. Así, si cambiara el formato de un archivo, solo se modifica una clase.

### Las cuatro entidades y cómo se relacionan

```
Evento (id) ──< Actividad (id, eventoId)
   │                 │
   │                 └──< Asistencia (actividadId, inscripcionId)
   └──< Inscripcion (id, eventoId) ──────────────┘
```

- Un **evento** tiene muchas **actividades** (su agenda) y muchas **inscripciones**.
- Una **asistencia** es una marca que une una actividad con una inscripción: "esta persona estuvo en esta actividad".
- Las relaciones se guardan con el **id** (`eventoId`, `actividadId`, `inscripcionId`), igual que las llaves foráneas de una base de datos.

### Los cuatro archivos

| Archivo | Formato de cada línea |
|---|---|
| `eventos.txt` | `id\|nombre\|categoria\|fechaHora\|lugar\|capacidad\|descripcion` |
| `actividades.txt` | `id\|eventoId\|nombre\|inicio\|fin` |
| `inscripciones.txt` | `id\|eventoId\|nombre\|documento\|estado\|fechaRegistro\|fechaPromocion` |
| `asistencias.txt` | `actividadId\|inscripcionId` |

---

## 3. Demostración paso a paso (con los datos de prueba)

Ejecuta `./mvnw spring-boot:run` y abre `http://localhost:8080`. Los datos de `datos/` ya cargan casos para todo. Sigue este orden; cada paso dice **qué hacer**, **qué decir** y **en qué clase está**.

**Paso 1 — Lista de eventos (`EventosView`).**
Hacer: mostrar la lista. Escribir "Taller" en *Buscar*; luego filtrar por categoría *Conferencia*; luego por una fecha. Hacer clic en una fila.
Decir: *"La búsqueda es en tiempo real: cada letra que escribo vuelve a filtrar. El estado no está guardado en ningún archivo, se calcula: el Taller de Java dice Lleno porque tiene 3 inscritos de 3 cupos, y la Conferencia de innovación dice Finalizado porque su última actividad ya terminó. La barra es un ProgressBar con el cupo ocupado."*

**Paso 2 — Crear un evento (`EventoFormView`).**
Hacer: *Nuevo evento*. Pulsar *Guardar* con todo vacío. Luego poner capacidad 0, luego una fecha pasada. Finalmente llenar bien y guardar.
Decir: *"Cada error sale en una Notification con un mensaje claro y se revisa antes de escribir en el archivo. El DateTimePicker me deja elegir fecha y hora en un solo campo."*

**Paso 3 — Agenda (`AgendaView`).**
Hacer: elegir *Taller de Java*. Intentar crear una actividad "Prueba" de 10:00 a 11:00 del 1 de diciembre de 2026.
Decir: *"El sistema rechaza el horario porque se cruza con 'Variables y tipos', que va de 9:00 a 10:30. La regla es: dos actividades se cruzan si cada una empieza antes de que la otra termine."* Luego crear una de 12:00 a 13:00, que sí pasa.

**Paso 4 — Inscripciones y lista de espera (`InscripcionesView`).**
Hacer: elegir *Taller de Java* (3 inscritos, 2 en espera). Inscribir a "Pedro Soto", documento `20020001`. Intentar de nuevo con el mismo documento. Probar con documento `abc`.
Decir: *"Como el evento está lleno, Pedro queda en lista de espera. Con el mismo documento me dice que ya está registrado: así evito inscripciones duplicadas. El documento se valida con una expresión regular: entre 5 y 15 dígitos."*

**Paso 5 — Bonificación: promoción automática (`InscripcionesView` + `ArchivoDatos`).**
Hacer: en *Taller de Java* cancelar a *Marta Ruiz* (confirmar el diálogo).
Decir: *"Al cancelar a una persona inscrita se libera un cupo, y el sistema llama a promoverPrimeroEnEspera. Carlos Díaz era el primero de la fila, así que pasa a inscrito, y la fecha y hora de la promoción quedan guardadas en el último campo de inscripciones.txt."* Cancelar ahora a alguien **En espera** (Diana): *"No promueve a nadie, porque esa persona no ocupaba cupo."*

**Paso 6 — Asistencia (`AsistenciaView`).**
Hacer: elegir *Conferencia de innovación* → *Sesión 1*. Se ven las casillas marcadas. Desmarcar una y guardar.
Decir: *"Es un CheckboxGroup. Solo aparecen los inscritos, no los que están en espera. Al guardar, se reemplazan todas las marcas de esa actividad por las que están seleccionadas."* Volver a marcarla y guardar, para dejar los datos como estaban.

**Paso 7 — Estadísticas (`EstadisticasView`).**
Decir: *"La ocupación es inscritos sobre capacidad. La asistencia es el total de marcas dividido entre inscritos por actividades: si hay 3 inscritos y 5 actividades, lo máximo posible son 15 marcas. En la Conferencia de innovación hay 12 de 15, o sea 80 %."*

**Paso 8 — Bonificación: certificado (`CertificadosView`).**
Hacer: elegir *Conferencia de innovación*. Mostrar Elena (100 %), Andrés (80 %), Sofía (60 %). Descargar el de Elena y abrirlo en el navegador.
Decir: *"Solo hay enlace de descarga si el porcentaje llega a 80. El HTML se genera en el momento de pulsar el enlace, no queda guardado en el servidor. El nombre pasa por un método que escapa caracteres especiales, para que un nombre con símbolos no dañe el documento."*

**Paso 9 — Borrar con confirmación (`EventosView`).**
Hacer: eliminar el evento que creaste en el paso 2.
Decir: *"Toda eliminación pasa por un diálogo de confirmación. Al borrar un evento también se borran su agenda, sus inscripciones y sus asistencias, para no dejar datos huérfanos."*

**Paso 10 — Celular.**
Hacer: F12 → modo dispositivo móvil. Abrir el menú de hamburguesa.
Decir: *"Los filtros de la lista están en un FlexLayout con wrap, así que bajan de línea; el FormLayout pasa a una sola columna; y el menú es un drawer que se oculta. Las tablas se desplazan horizontalmente."*

> **Para volver a empezar:** detén la aplicación, borra la carpeta `datos/` y vuelve a ejecutar. La aplicación recrea datos de ejemplo (con fechas relativas a hoy). O restaura los `.txt` originales del proyecto.

---

## 4. Explicación clase por clase

Todas están en el paquete `com.eventos`. Primero las que tienen datos, luego la clase de archivos, luego las vistas.

### 4.1 `EventosApplication` — punto de entrada
- Tiene el `main`, que arranca Spring Boot con `SpringApplication.run`.
- `@SpringBootApplication` marca la clase de arranque. `@StyleSheet(Lumo.STYLESHEET)` carga el tema visual Lumo de Vaadin. Implementa `AppShellConfigurator` porque ahí se configuran cosas globales de la página.
- *"Es la puerta de entrada: no tiene lógica de mi proyecto, solo enciende el servidor y define el tema."*

### 4.2 `Evento` — modelo del evento
- **Atributos:** `id`, `nombre`, `categoria`, `fechaHora` (`LocalDateTime`), `lugar`, `capacidad`, `descripcion`.
- **Constantes:** `ABIERTO`, `LLENO`, `FINALIZADO`; `CATEGORIAS` (Taller y Conferencia, la lista que usan los ComboBox); `FORMATO_FECHA` (`dd/MM/yyyy HH:mm`).
- **`getEstado()`** — el método más importante. No lee un campo guardado, lo **calcula**:
  1. Busca la agenda del evento. El "fin" del evento es el fin de la última actividad; si no hay agenda, es la propia fecha del evento.
  2. Si ahora es posterior a ese fin → `Finalizado`.
  3. Si no, y los inscritos son iguales o superiores a la capacidad → `Lleno`.
  4. Si no → `Abierto`.
  El orden importa: un evento ya terminado dice Finalizado aunque haya estado lleno.
- **`getInscritos()`** — cuenta solo las inscripciones con estado `Inscrito` (las que están en espera no ocupan cupo).
- **`getOcupacion()`** — inscritos / capacidad, limitado a 1.0. Es el valor que recibe la `ProgressBar`.
- **`getAsistencia()`** — marcas de asistencia / (inscritos × número de actividades), limitado a 1.0. Si no hay inscritos o actividades devuelve 0 (evita dividir entre cero).
- **`getFechaTexto()`** — la fecha ya formateada para mostrarla.
- *"¿Por qué no guardo el estado en el archivo? Porque cambiaría con el tiempo y con cada inscripción; si lo guardara, me quedaría desactualizado. Calculándolo siempre es correcto."*

### 4.3 `Actividad` — modelo de la agenda
- **Atributos:** `id`, `eventoId` (a qué evento pertenece), `nombre`, `inicio`, `fin`.
- `getInicioTexto()` y `getFinTexto()` formatean las fechas para las tablas.
- Es una clase de datos simple. Su lógica (no solaparse) vive en `AgendaView.validar`.

### 4.4 `Inscripcion` — modelo de la inscripción
- **Atributos:** `id`, `eventoId`, `nombre`, `documento`, `estado`, `fechaRegistro` y `fechaPromocion`.
- **Constantes:** `INSCRITO` y `EN_ESPERA`.
- **`fechaPromocion`** es `null` mientras la persona no haya pasado por la lista de espera. Eso es lo que registra "cuándo" fue promovida (bonificación).
- **`promover(momento)`** — cambia el estado a `Inscrito` y guarda el momento.
- **`getEstadoTexto()`** — para la tabla: devuelve solo el estado, o `Inscrito (promovido dd/MM/aaaa HH:mm)` si tiene fecha de promoción.

### 4.5 `Asistencia` — modelo de la marca
- Solo dos campos: `actividadId` e `inscripcionId`. Una línea de `asistencias.txt` = "esta inscripción asistió a esta actividad".
- Por eso `asistencias.txt` solo tiene dos columnas: es una tabla de relación (muchos a muchos entre actividades e inscripciones).

### 4.6 `ArchivoDatos` — lee y escribe los archivos
La clase más grande. Todos sus métodos son `static`, así que se usan sin crear objetos (`ArchivoDatos.listarEventos()`). Está organizada en secciones por tipo de dato.

**Patrón que se repite en cada tipo de dato:**

| Operación | Qué hace |
|---|---|
| `listarX()` | Si el archivo no existe, crea datos de ejemplo. Lee línea por línea; cada línea va en un `try/catch` y si está mal escrita se ignora con un mensaje por consola. |
| `guardarX(x)` | Si el `id` es 0 es un registro nuevo: busca el mayor id existente y le suma 1. Si no, reemplaza el registro que tiene ese id. Luego reescribe el archivo completo. |
| `eliminarX(id)` | Quita el registro (y lo que dependa de él) y reescribe. |
| `escribirX(lista)` | Convierte cada objeto a una línea con `\|` entre campos y llama a `escribirLineas`. |
| `crearXDeEjemplo()` | Crea datos de ejemplo con fechas relativas a hoy. Solo se usa si el archivo no existe. |

**Utilidades (al final de la clase):**
- **`separar(linea, cantidad)`** — parte la línea por `|` con `split("\\|", -1)` y lanza una excepción si no hay exactamente la cantidad de campos esperada. El `-1` es clave: sin él, Java descarta los campos vacíos del final, y el último campo de `inscripciones.txt` (la fecha de promoción) *normalmente está vacío*. Esa excepción la atrapa el `try/catch` de `listarX`, y así una línea dañada no tumba la aplicación.
- **`leerLineas(archivo)`** — `Files.readAllLines`, ignorando líneas en blanco; si hay un `IOException` lo atrapa y devuelve lista vacía.
- **`escribirLineas(archivo, lineas)`** — crea la carpeta `datos/` si no existe, escribe con `Files.write` y devuelve `true` o `false`. Las vistas usan ese booleano para decir "No se pudo guardar".
- **`hoyALasNueve()`** — hoy a las 9:00, base para las fechas relativas de los ejemplos.

**Métodos que conviene saber explicar:**
- **`eliminarEvento(id)`** — borra en cascada: primero las marcas de asistencia de sus actividades, luego sus actividades, sus inscripciones y por último el evento. Así no quedan datos huérfanos.
- **`eliminarActividad(id)`** — quita la actividad y sus marcas de asistencia.
- **`cancelarInscripcion(id)`** — quita la inscripción y sus marcas de asistencia.
- **`guardarAsistentes(actividadId, ids)`** — primero borra todas las marcas de esa actividad y luego agrega una por cada id recibido. Es "reemplazar", no "sumar", y por eso desmarcar una casilla y guardar funciona.
- **`listarActividades(eventoId)`** — la agenda de un evento, ordenada por hora de inicio con un `Comparator`.
- **`promoverPrimeroEnEspera(eventoId)`** *(bonificación)* — recorre las inscripciones **en el orden del archivo** (que es el orden de llegada) y promueve a la primera persona del evento que esté En espera, con la hora actual. Devuelve esa inscripción, o `null` si no había nadie esperando.
- **`promoverSegunCupo(eventoId, capacidad)`** *(mejora añadida)* — cuenta los inscritos y, mientras haya cupo libre, promueve a las personas en espera en orden. Se usa cuando alguien edita un evento y sube la capacidad.

*"Hay dos versiones de `listarActividades` y de `listarInscripciones`: una privada sin parámetros que devuelve todo el archivo, y una pública que recibe un id de evento y filtra. Es sobrecarga de métodos."*

### 4.7 `MainLayout` — la plantilla común
- Extiende `AppLayout`: barra superior y menú lateral (drawer) compartidos por todas las vistas.
- El constructor arma el título, el `DrawerToggle` (botón de hamburguesa) y un `SideNav` con 7 `SideNavItem`, uno por vista.
- **`avisar(mensaje)`** — método estático que muestra una `Notification` de 4 segundos en el centro. Todas las vistas lo usan, para que los mensajes se vean igual en todo el programa.
- **`confirmar(mensaje, accion)`** — método estático que abre un `Dialog` con "Sí" y "No". Recibe la acción como un `Runnable`, o sea un trozo de código, y solo lo ejecuta si el usuario pulsa Sí.
- *"Puse avisar y confirmar aquí para no repetir el mismo código en cada vista."*
- Cada vista se conecta a este layout con `@Route(value = "...", layout = MainLayout.class)`.

### 4.8 `EventosView` — lista de eventos (ruta `""`, la de inicio)
- **Qué muestra:** un `Grid<Evento>` con columnas Evento, Fecha, Estado, Cupo y Acciones.
- **Búsqueda en tiempo real:** el `TextField` usa `ValueChangeMode.EAGER`, que dispara el evento con cada tecla.
- **Filtros:** `ComboBox` de categoría y `DatePicker` de fecha. Los tres controles llaman al mismo método, `actualizarGrid()`.
- **`actualizarGrid()`** — recorre todos los eventos y deja los que cumplen **las tres** condiciones a la vez (nombre contiene el texto, categoría coincide o no hay filtro, fecha coincide o no hay filtro). Si un filtro está vacío (`null`) se considera que "coincide".
- **`crearCupo(evento)`** — columna de componentes con un texto "inscritos / capacidad" y una `ProgressBar`. **Aquí se usa la ProgressBar.**
- **`crearAcciones(evento)`** — botones Editar (navega al formulario pasándole el id) y Eliminar (pasa por `MainLayout.confirmar`).
- **Detalle de fila:** `setItemDetailsRenderer` despliega lugar, capacidad y descripción al hacer clic.
- **Celular:** los filtros están en un `FlexLayout` con `FlexWrap.WRAP`.

### 4.9 `EventoFormView` — crear y editar evento (ruta `evento`)
- Implementa `HasUrlParameter<Integer>`: la URL puede traer un id opcional (`/evento/3`). El método `setParameter` se ejecuta al entrar: si no hay id es "nuevo evento"; si hay id, busca el evento, rellena los campos y cambia el título a "Editar evento".
- **Campos:** `TextField`, `ComboBox`, **`DateTimePicker`** (fecha y hora en uno solo), `IntegerField` (con `setMin`/`setMax`) y `TextArea`.
- **`validar()`** devuelve el mensaje del primer error o `null` si todo está bien. Revisa: obligatorios, fecha no pasada (solo en eventos nuevos), capacidad 1–10 000, que al editar la capacidad no sea menor que los inscritos actuales, que la fecha no quede después de una actividad ya creada, y que no haya `|` (que rompería el formato del archivo).
- **`guardar()`** — valida; si hay error, lo muestra y se detiene. Si todo está bien, arma el `Evento`, reemplaza los saltos de línea de la descripción por espacios (otro riesgo para el formato de una línea por registro) y llama a `ArchivoDatos.guardarEvento`. Si editó el evento, intenta promover a quienes ahora caben en la lista de espera.
- **Celular:** `FormLayout` pasa a una columna en pantallas angostas.

### 4.10 `AgendaView` — actividades de un evento (ruta `agenda`)
- Primero se elige el evento en un `ComboBox`; el grid muestra solo sus actividades.
- Formulario con `TextField` y dos **`DateTimePicker`** (inicio y fin). Sirve para crear y para editar: la variable `actividadEditada` es `null` si es nueva y guarda la actividad si se está editando.
- **`validar()`** — revisa evento elegido, nombre obligatorio sin `|`, inicio y fin obligatorios, **fin posterior al inicio**, que no empiece antes del evento y, lo importante, **que no se cruce con otra actividad**:
  ```java
  boolean seCruzan = inicio.isBefore(otra.getFin()) && otra.getInicio().isBefore(fin);
  ```
  *"Dos intervalos se solapan si cada uno empieza antes de que el otro termine. Si una termina justo cuando la otra empieza, no hay cruce."* Al editar, se excluye a la propia actividad de la comparación (`esLaMisma`) para que no choque consigo misma.

### 4.11 `InscripcionesView` — inscribir y cancelar (ruta `inscripciones`)
- **`inscribir()`** — valida y decide el estado: si `inscritos < capacidad` → `Inscrito`; si no → `En espera`. Guarda y avisa qué pasó (con un mensaje distinto en cada caso).
- **`validar()`** — evento elegido y no finalizado, nombre obligatorio, documento con `matches("[0-9]{5,15}")`, y que **ese documento no esté ya** en las inscripciones del mismo evento (evita duplicados).
- **Cancelar:** el botón primero revisa que el evento no haya finalizado, y luego pide confirmación. **`cancelar()`** elimina la inscripción y, **solo si la persona estaba Inscrita**, llama a `promoverPrimeroEnEspera`. Si hay promovida, el mensaje dice su nombre.
- **`actualizar()`** — recarga el grid y el resumen: "Estado: Lleno. Inscritos: 3 de 3. En espera: 2." (lo que no son inscritos se cuenta como en espera).

### 4.12 `AsistenciaView` — marcar asistencia (ruta `asistencia`)
- Dos `ComboBox` encadenados: al cambiar el evento, `cambiarEvento()` carga sus actividades; al elegir actividad, `cargarAsistentes()` llena el grupo.
- **`CheckboxGroup<Inscripcion>`** — **aquí se usa el CheckboxGroup.** Solo se cargan las inscripciones con estado `Inscrito`. Se marcan las que ya tienen asistencia guardada, y para eso se arma un `Set` de la selección inicial. Se muestra en vertical (`LUMO_VERTICAL`).
- **`guardar()`** — toma los seleccionados, extrae sus ids y llama a `guardarAsistentes`, que reemplaza las marcas de esa actividad.

### 4.13 `EstadisticasView` — ocupación y asistencia (ruta `estadisticas`)
- Un grid con una fila por evento y dos columnas con `ProgressBar`: ocupación y asistencia.
- **`crearIndicador(fraccion)`** recibe un número de 0 a 1 y lo muestra como porcentaje (`Math.round(fraccion * 100)`) y como barra. Se reutiliza para las dos columnas, así no se repite código.
- Los cálculos están en `Evento.getOcupacion()` y `Evento.getAsistencia()`.

### 4.14 `CertificadosView` — certificados (ruta `certificados`) — bonificación
- Se elige el evento y el grid muestra, por cada **inscrito**, su porcentaje de asistencia, las horas y el enlace de certificado.
- **`actividadesAsistidas(inscripcion)`** — las actividades del evento a las que la persona tiene marca.
- **`porcentaje(inscripcion)`** — `asistidas * 100 / total`. Si el evento no tiene agenda devuelve 0, así que no genera certificado. *Se multiplica antes de dividir porque la división de enteros descarta decimales: 4 * 100 / 5 = 80 exacto, mientras que 4 / 5 * 100 daría 0.*
- **`textoHoras(inscripcion)`** — suma con `Duration.between(inicio, fin).toMinutes()` la duración de las actividades asistidas y la convierte a horas con un decimal.
- **`crearDescarga(inscripcion)`** — si el porcentaje es menor que 80 devuelve un `Span` "No disponible". Si no, devuelve un `Anchor` con un **`DownloadHandler`** (`fromInputStream`) y `AttachmentType.DOWNLOAD`. El contenido se genera **cuando se pulsa el enlace**: no se crea ningún archivo en el servidor.
- **`crearCertificado(...)`** — un bloque de texto (`"""`) con el HTML y marcadores `%s` que se llenan con `formatted`. Incluye estilos para que se vea bien al imprimir.
- **`escapar(texto)`** — cambia `&`, `<`, `>`, `"` y `'` por sus entidades HTML. *"Si alguien se inscribe con un nombre que contenga código HTML, no se interpreta, se muestra como texto. Es una medida de seguridad."*

---

## 5. Dónde está cada requisito del enunciado

### Núcleo común
| # | Requisito | Dónde |
|---|---|---|
| 1 | CRUD completo | Eventos: `EventosView` + `EventoFormView`. Actividades: `AgendaView`. Inscripciones: `InscripcionesView`. |
| 2 | Grid con búsqueda y filtro | `EventosView.actualizarGrid` — `TextField` EAGER + `ComboBox` + `DatePicker` |
| 3 | ≥ 3 vistas con `@Route` y `AppLayout` | 7 vistas + `MainLayout` con `SideNav` |
| 4 | Validación con `Notification` | Método `validar()` en cada formulario + `MainLayout.avisar` |
| 5 | Confirmar antes de borrar | `MainLayout.confirmar`, usado en eventos, actividades y cancelación de inscripciones |
| 6 | Persistencia y errores | `ArchivoDatos`: `try/catch` por línea, `separar`, creación de ejemplos, booleanos de éxito |
| 7 | Los 3 componentes | `ProgressBar`: `EventosView.crearCupo` y `EstadisticasView`. `DateTimePicker`: `EventoFormView` y `AgendaView`. `CheckboxGroup`: `AsistenciaView`. |

### Proyecto 11
| Requisito | Dónde |
|---|---|
| Evento con nombre, categoría, fecha y hora, lugar, capacidad y descripción | `Evento` + `EventoFormView` |
| Estado calculado (Abierto, Lleno, Finalizado) | `Evento.getEstado` |
| Agenda sin solapamientos | `AgendaView.validar` |
| Cupo y lista de espera | `InscripcionesView.inscribir` |
| Nadie se inscribe dos veces | `InscripcionesView.validar` (compara documentos) |
| Asistencia por actividad | `AsistenciaView` + `ArchivoDatos.guardarAsistentes` |
| Búsqueda y filtro por fecha y categoría | `EventosView` |
| Estadísticas de ocupación y asistencia | `EstadisticasView` + `Evento.getOcupacion/getAsistencia` |
| **Bonificación:** promoción automática con fecha registrada | `InscripcionesView.cancelar` → `ArchivoDatos.promoverPrimeroEnEspera` → `Inscripcion.promover` |
| **Bonificación:** certificado HTML al 80 % | `CertificadosView` |

---

## 6. Preguntas probables y cómo responderlas

**¿Por qué los métodos de `ArchivoDatos` son `static`?**
Porque la clase no guarda estado propio: solo agrupa operaciones sobre archivos. Así las vistas la llaman sin crear objetos. (Contrapartida: es más difícil de probar y de reemplazar por una base de datos que si fuera una interfaz.)

**¿Por qué el estado del evento no está en el archivo?**
Porque depende de la hora actual y del número de inscritos, que cambian. Si lo guardara quedaría desactualizado; calculándolo en `getEstado` siempre es correcto.

**¿Qué pasa si el `.txt` tiene una línea dañada o no existe?**
Si no existe, la aplicación crea datos de ejemplo. Si una línea está mal (campos de más o de menos, fecha inválida, número que no es número), `separar` o el `parse` lanzan una excepción que atrapa el `try/catch`, se imprime un aviso por consola y se sigue con las demás líneas. La aplicación no se cae.

**¿Por qué `split("\\|", -1)`?**
La barra `|` es un carácter especial en expresiones regulares, por eso se escapa con `\\`. El `-1` evita que Java descarte los campos vacíos finales; sin él, una inscripción sin fecha de promoción tendría 6 campos en vez de 7.

**¿Cómo evitas que un texto con `|` dañe el archivo?**
Los formularios rechazan los textos que contengan `|`, y en la descripción se reemplazan los saltos de línea por espacios, porque cada registro debe ocupar una sola línea.

**¿Cómo se calcula el porcentaje de asistencia de una persona?**
Actividades a las que asistió × 100 / total de actividades del evento, con enteros. Se multiplica primero para no perder precisión. Con 4 de 5 da exactamente 80, que es el mínimo para el certificado.

**¿Cómo funciona la promoción de la lista de espera?**
Las inscripciones se guardan en orden de llegada, así que la primera línea del evento con estado "En espera" es la primera de la fila. Al cancelar a una persona inscrita se libera un cupo y se llama a `promoverPrimeroEnEspera`, que cambia ese estado y guarda la fecha. Cancelar a alguien que estaba en espera no libera cupo, por eso no promueve.

**¿Qué es `HasUrlParameter` y para qué lo usaste?**
Es una interfaz de Vaadin para que una vista reciba un parámetro por la URL. La uso en `EventoFormView` para que el mismo formulario sirva para crear (`/evento`) y editar (`/evento/3`). `@OptionalParameter` indica que el id puede no venir.

**¿Para qué sirve `ValueChangeMode.EAGER`?**
Hace que el campo de texto avise a la aplicación con cada tecla, y no solo cuando pierde el foco. Es lo que da la búsqueda "en tiempo real".

**¿Qué es el `Runnable` de `MainLayout.confirmar`?**
Es un bloque de código que se pasa como parámetro. `confirmar` muestra el diálogo y ejecuta ese bloque solo si el usuario pulsa Sí. Así un solo diálogo sirve para eliminar eventos, actividades o inscripciones.

**¿Cómo se genera el certificado sin guardar un archivo?**
Con un `DownloadHandler.fromInputStream`: se arma un `DownloadResponse` con los bytes del HTML, el nombre del archivo y el tipo `text/html`. Se ejecuta en el momento en que el usuario pulsa el enlace.

**¿Para qué sirve `escapar`?**
Para que el nombre de la persona se muestre como texto y no se interprete como HTML. Sin eso, alguien podría inscribirse con un nombre que contenga etiquetas y alterar el certificado.

**¿Funciona en el celular?**
La interfaz se adapta: menú lateral que se oculta, `FormLayout` de una sola columna en pantallas angostas y filtros en `FlexLayout` con salto de línea. Las tablas tienen desplazamiento horizontal. Lo probé con el modo dispositivo móvil del navegador.

**¿Qué mejorarías?**
Ver sección 7.

---

## 7. Limitaciones que debes conocer

Mejor decirlas tú antes de que te las pregunten:

- **Rendimiento:** cada vez que se pide el estado, la ocupación o la asistencia de un evento, se vuelven a leer los archivos. Con pocos datos no se nota, pero con miles de registros sería lento. Una base de datos o un caché lo resolvería.
- **Usuarios simultáneos:** `ArchivoDatos` lee el archivo completo, modifica la lista y lo reescribe completo, sin bloqueos. Si dos personas guardan al mismo tiempo, una podría pisar el cambio de la otra. Para un proyecto individual de clase es aceptable.
- **Inscripciones:** se pueden crear y cancelar, pero no editar; para corregir un dato hay que cancelar y volver a inscribir.
- **Identificación de personas:** la persona se identifica por nombre y documento dentro de cada evento; no existe una tabla aparte de "personas".
- **Sin autenticación:** cualquier persona que abra la aplicación puede hacer todo. No era un requisito.
- **Fechas fijas en los datos de ejemplo:** los `.txt` entregados usan fechas concretas (diciembre de 2026 y enero de 2027). Cuando pasen, esos eventos se verán como Finalizados. Borrando la carpeta `datos/` la aplicación recrea ejemplos con fechas relativas a hoy.
- **Qué no pude verificar al preparar este paquete:** la capa de datos (`ArchivoDatos` y las clases modelo) se probó con una prueba automática contra los `.txt` de ejemplo. Las vistas de Vaadin las revisé leyendo el código, pero hay que ejecutar la aplicación y recorrer los pasos de la sección 3 antes de sustentar.
