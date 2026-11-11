# Eventos e inscripciones

Aplicación web hecha con **Vaadin 25 y Java 21** para una organización que dicta talleres y conferencias con cupo limitado. Reemplaza las inscripciones en papel: controla el cupo, manda a la lista de espera cuando un evento se llena, registra la asistencia por actividad, muestra estadísticas y genera certificados.

- **Proyecto:** Proyecto final, Lógica de Programación (proyecto 11 de 15).
- **Autor:** Samuel Chica Vélez — ID 000279346
- **Fecha de entrega:** 11 de noviembre de 2026

---

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje |
| Vaadin 25.3 (Flow) | Interfaz web, solo componentes gratuitos |
| Spring Boot 4 | Arranque de la aplicación (creada con Spring Initializr) |
| Maven (`mvnw`) | Compilación y ejecución |
| Archivos `.txt` separados por `\|` | Persistencia (sin base de datos) |

No se usan componentes de pago (Charts, Board, Grid Pro, CRUD, Dashboard), ni add-ons del Vaadin Directory, ni otras librerías de interfaz.

---

## Cómo ejecutarlo

Requisitos: **JDK 21** instalado.

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
mvnw.cmd spring-boot:run
```

La primera vez descarga dependencias y prepara el frontend, así que puede tardar unos minutos. Cuando termine, abre <http://localhost:8080>.

> **Importante:** ejecuta el comando desde la carpeta raíz del proyecto (donde está `pom.xml`), porque la aplicación lee y escribe la carpeta `datos/` en la ruta desde donde se lanza.

### Probar en el celular

Con la aplicación corriendo, en Chrome pulsa **F12** y luego el ícono de dispositivos móviles (o `Ctrl+Shift+M`) para ver la interfaz con pantalla de celular. También puedes abrir `http://IP-DE-TU-PC:8080` desde un celular conectado a la misma red.

---

## Datos de ejemplo

La carpeta `datos/` trae cuatro archivos listos para probar todo sin llenar nada a mano:

| Archivo | Contenido de ejemplo |
|---|---|
| `eventos.txt` | 5 eventos: uno **Lleno** (Taller de Java, cupo 3), tres **Abiertos**, uno **Finalizado** (Conferencia de innovación) y uno sin agenda ni inscritos (Taller de hojas de cálculo) |
| `actividades.txt` | 11 actividades repartidas en los eventos 1 a 4 |
| `inscripciones.txt` | 12 inscripciones; el evento 1 tiene 3 inscritos y 2 en lista de espera, y una persona ya promovida |
| `asistencias.txt` | Asistencia del evento finalizado: Elena 5 de 5, Andrés 4 de 5 y Sofía 3 de 5 |

**Si borras la carpeta `datos/` o alguno de sus archivos**, la aplicación vuelve a crear datos de ejemplo al primer arranque, con fechas calculadas a partir del día de hoy. Es útil si las fechas de los datos fijos ya pasaron y los eventos aparecen como Finalizados.

### Formato de los archivos

Cada tipo de dato vive en su propio `.txt`, un registro por línea, con los campos separados por `|`:

```
eventos.txt         id|nombre|categoria|fechaHora|lugar|capacidad|descripcion
actividades.txt     id|eventoId|nombre|inicio|fin
inscripciones.txt   id|eventoId|nombre|documento|estado|fechaRegistro|fechaPromocion
asistencias.txt     actividadId|inscripcionId
```

Las fechas usan el formato ISO (`2026-12-01T09:00`). En `inscripciones.txt` el último campo (`fechaPromocion`) queda vacío si la persona nunca estuvo en lista de espera. Una línea mal escrita no tumba la aplicación: se ignora y se avisa por consola.

---

## Manual de uso

El menú lateral (botón de hamburguesa arriba a la izquierda) tiene siete opciones.

### 1. Eventos
Pantalla de inicio. Lista todos los eventos con su nombre, categoría, fecha, **estado** y una **barra de cupo** (inscritos sobre capacidad).
- **Buscar:** escribe en el campo y la lista se filtra mientras tecleas.
- **Filtros:** por categoría (Taller o Conferencia) y por fecha.
- **Detalle:** al hacer clic en una fila se despliega el lugar, la capacidad y la descripción.
- **Editar / Eliminar:** eliminar pide confirmación y borra también la agenda, las inscripciones y las asistencias del evento.

![Lista de eventos](docs/capturas/01-eventos.png)

### 2. Nuevo evento
Formulario para crear un evento (o editarlo, si vienes desde el botón *Editar*). Todos los campos son obligatorios. Se valida que:
- la fecha de un evento nuevo no esté en el pasado,
- la capacidad esté entre 1 y 10 000,
- al editar, la capacidad no sea menor que los inscritos actuales ni la fecha posterior a alguna actividad,
- los textos no contengan el carácter `|`.

Los errores aparecen en una notificación en el centro de la pantalla.

![Formulario de evento](docs/capturas/02-formulario-evento.png)

### 3. Agenda
Elige un evento y administra sus actividades (nombre, inicio y fin con un selector de fecha y hora). El sistema **rechaza actividades que se solapan** con otra del mismo evento, que terminan antes de empezar o que empiezan antes que el evento.

![Agenda](docs/capturas/03-agenda.png)

### 4. Inscripciones
Elige un evento, escribe nombre y documento (5 a 15 dígitos) y pulsa *Inscribir*.
- Si hay cupo, la persona queda **Inscrito**.
- Si el evento está lleno, queda **En espera** y se avisa.
- Nadie puede inscribirse dos veces al mismo evento (se compara el documento).
- No se admiten inscripciones en eventos finalizados.
- Al **cancelar** una inscripción se pide confirmación.

![Inscripciones](docs/capturas/04-inscripciones.png)

### 5. Asistencia
Elige evento y actividad y marca con casillas quiénes asistieron (solo aparecen los inscritos). Pulsa *Guardar asistencia*.

![Asistencia](docs/capturas/05-asistencia.png)

### 6. Estadísticas
Por cada evento muestra el **porcentaje de ocupación** (inscritos / capacidad) y el **porcentaje de asistencia** (marcas / (inscritos × actividades)), cada uno con su barra de progreso.

![Estadísticas](docs/capturas/06-estadisticas.png)

### 7. Certificados
Elige un evento y verás, por cada inscrito, su porcentaje de asistencia y las horas asistidas. Quien llegue al **80 %** o más tiene un enlace *Descargar*; los demás ven "No disponible".

![Certificados](docs/capturas/07-certificados.png)

---

## Bonificación (+0.5): promoción automática y certificado

### Cómo probar la promoción automática
1. Ve a **Inscripciones** y elige *Taller de Java*: tiene 3 inscritos (cupo lleno) y 2 personas en espera (Carlos Díaz y Diana Mora).
2. Cancela a cualquier persona **Inscrita** (por ejemplo, Marta Ruiz) y confirma.
3. La notificación dice quién pasó de la lista de espera a inscrito. En la tabla, Carlos Díaz aparece como `Inscrito (promovido dd/MM/aaaa HH:mm)`: la fecha y hora quedan guardadas en `inscripciones.txt`.
4. Cancelar a una persona que está **En espera** no promueve a nadie, porque no libera cupo.
5. Extra: si en *Editar evento* subes la capacidad, las personas en espera que ahora caben también pasan a inscritas.

### Cómo probar el certificado
1. Ve a **Certificados** y elige *Conferencia de innovación* (evento finalizado, con 5 actividades de una hora).
2. **Elena Castro** (100 %) y **Andrés Vega** (80 %) tienen el enlace *Descargar*. **Sofía León** (60 %) muestra "No disponible".
3. Al descargar se obtiene un `.html` con el nombre, el evento, la fecha y las horas asistidas. Ábrelo en el navegador y pulsa `Ctrl+P` para imprimirlo o guardarlo como PDF.

---

## Estructura del proyecto

```
src/main/java/com/eventos/
├── EventosApplication.java   Punto de entrada y tema visual
├── MainLayout.java           Menú lateral, mensajes y diálogo de confirmación
├── Evento.java               Modelo: evento (estado, ocupación y asistencia calculados)
├── Actividad.java            Modelo: actividad de la agenda
├── Inscripcion.java          Modelo: inscripción (inscrito o en espera)
├── Asistencia.java           Modelo: marca de asistencia
├── ArchivoDatos.java         Lectura y escritura de los .txt
├── EventosView.java          Vista: lista, búsqueda, filtros, cupo
├── EventoFormView.java       Vista: crear y editar evento
├── AgendaView.java           Vista: actividades de un evento
├── InscripcionesView.java    Vista: inscribir, lista de espera, cancelar
├── AsistenciaView.java       Vista: marcar asistencia
├── EstadisticasView.java     Vista: ocupación y asistencia
└── CertificadosView.java     Vista: certificados descargables
datos/                        Archivos .txt con los datos de ejemplo
docs/GUION_SUSTENTACION.md    Explicación del código para la sustentación
```

La explicación detallada de cada clase está en [`docs/GUION_SUSTENTACION.md`](docs/GUION_SUSTENTACION.md).

---

## Cumplimiento del enunciado

| Requisito | Dónde está |
|---|---|
| CRUD completo | `EventosView`, `EventoFormView`, `AgendaView`, `InscripcionesView` |
| Grid con búsqueda y filtro | `EventosView` (`TextField`, `ComboBox` y `DatePicker`) |
| Al menos 3 vistas con `@Route` y `AppLayout` | 7 vistas + `MainLayout` |
| Validación con `Notification` | `validar()` de cada formulario y `MainLayout.avisar` |
| Confirmar antes de borrar | `MainLayout.confirmar` |
| Persistencia y errores | `ArchivoDatos` (`try/catch`, creación de ejemplos) |
| `ProgressBar`, `DateTimePicker`, `CheckboxGroup` | Cupo y estadísticas; evento y agenda; asistencia |
| Estado calculado, agenda sin solapes, cupo y lista de espera | `Evento.getEstado`, `AgendaView.validar`, `InscripcionesView.inscribir` |
| Bonificación | `ArchivoDatos.promoverPrimeroEnEspera` y `CertificadosView` |
