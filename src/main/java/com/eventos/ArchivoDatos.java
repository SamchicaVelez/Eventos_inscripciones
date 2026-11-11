/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Lee y escribe los archivos de texto de la aplicación.
public class ArchivoDatos {

    private static final Path CARPETA = Path.of("datos");
    private static final Path EVENTOS = CARPETA.resolve("eventos.txt");
    private static final Path ACTIVIDADES = CARPETA.resolve("actividades.txt");
    private static final Path INSCRIPCIONES = CARPETA.resolve("inscripciones.txt");
    private static final Path ASISTENCIAS = CARPETA.resolve("asistencias.txt");

    // ---------- Eventos ----------

    public static List<Evento> listarEventos() {
        if (!Files.exists(EVENTOS)) {
            crearEventosDeEjemplo();
        }
        List<Evento> eventos = new ArrayList<>();
        for (String linea : leerLineas(EVENTOS)) {
            try {
                String[] campos = separar(linea, 7);
                eventos.add(new Evento(Integer.parseInt(campos[0]), campos[1], campos[2],
                        LocalDateTime.parse(campos[3]), campos[4],
                        Integer.parseInt(campos[5]), campos[6]));
            } catch (RuntimeException e) {
                System.out.println("Línea ignorada en eventos.txt: " + linea);
            }
        }
        return eventos;
    }

    public static Evento buscarEvento(int id) {
        for (Evento evento : listarEventos()) {
            if (evento.getId() == id) {
                return evento;
            }
        }
        return null;
    }

    // Si el id es 0 agrega el evento; si no, reemplaza el que tiene ese id.
    public static boolean guardarEvento(Evento evento) {
        List<Evento> eventos = listarEventos();
        if (evento.getId() == 0) {
            int mayor = 0;
            for (Evento otro : eventos) {
                mayor = Math.max(mayor, otro.getId());
            }
            evento.setId(mayor + 1);
            eventos.add(evento);
        } else {
            for (int i = 0; i < eventos.size(); i++) {
                if (eventos.get(i).getId() == evento.getId()) {
                    eventos.set(i, evento);
                }
            }
        }
        return escribirEventos(eventos);
    }

    // Elimina el evento junto con su agenda, inscripciones y asistencias.
    public static boolean eliminarEvento(int id) {
        List<Actividad> actividades = listarActividades();
        List<Asistencia> asistencias = listarAsistencias();
        for (int i = actividades.size() - 1; i >= 0; i--) {
            if (actividades.get(i).getEventoId() == id) {
                quitarMarcasDeActividad(asistencias, actividades.get(i).getId());
                actividades.remove(i);
            }
        }
        List<Inscripcion> inscripciones = listarInscripciones();
        for (int i = inscripciones.size() - 1; i >= 0; i--) {
            if (inscripciones.get(i).getEventoId() == id) {
                inscripciones.remove(i);
            }
        }
        List<Evento> eventos = listarEventos();
        for (int i = eventos.size() - 1; i >= 0; i--) {
            if (eventos.get(i).getId() == id) {
                eventos.remove(i);
            }
        }
        return escribirAsistencias(asistencias) && escribirActividades(actividades)
                && escribirInscripciones(inscripciones) && escribirEventos(eventos);
    }

    private static boolean escribirEventos(List<Evento> eventos) {
        List<String> lineas = new ArrayList<>();
        for (Evento e : eventos) {
            lineas.add(e.getId() + "|" + e.getNombre() + "|" + e.getCategoria() + "|"
                    + e.getFechaHora() + "|" + e.getLugar() + "|" + e.getCapacidad() + "|"
                    + e.getDescripcion());
        }
        return escribirLineas(EVENTOS, lineas);
    }

    // Las fechas dependen de hoy para que los ejemplos siempre tengan sentido.
    private static void crearEventosDeEjemplo() {
        LocalDateTime hoy = hoyALasNueve();
        List<Evento> eventos = new ArrayList<>();
        eventos.add(new Evento(1, "Taller de Java", "Taller", hoy.plusDays(7),
                "Sala 101", 3, "Introducción a la programación en Java."));
        eventos.add(new Evento(2, "Conferencia de datos", "Conferencia", hoy.plusDays(14),
                "Auditorio principal", 50, "Usos del análisis de datos en las empresas."));
        eventos.add(new Evento(3, "Taller de fotografía", "Taller", hoy.plusDays(21),
                "Sala 203", 15, "Técnicas básicas de fotografía digital."));
        eventos.add(new Evento(4, "Conferencia de innovación", "Conferencia", hoy.minusDays(10),
                "Auditorio principal", 40, "Tendencias de innovación en la región."));
        escribirEventos(eventos);
    }

    // ---------- Actividades ----------

    private static List<Actividad> listarActividades() {
        if (!Files.exists(ACTIVIDADES)) {
            crearActividadesDeEjemplo();
        }
        List<Actividad> actividades = new ArrayList<>();
        for (String linea : leerLineas(ACTIVIDADES)) {
            try {
                String[] campos = separar(linea, 5);
                actividades.add(new Actividad(Integer.parseInt(campos[0]), Integer.parseInt(campos[1]),
                        campos[2], LocalDateTime.parse(campos[3]), LocalDateTime.parse(campos[4])));
            } catch (RuntimeException e) {
                System.out.println("Línea ignorada en actividades.txt: " + linea);
            }
        }
        return actividades;
    }

    // Agenda de un evento, ordenada por hora de inicio.
    public static List<Actividad> listarActividades(int eventoId) {
        List<Actividad> agenda = new ArrayList<>();
        for (Actividad actividad : listarActividades()) {
            if (actividad.getEventoId() == eventoId) {
                agenda.add(actividad);
            }
        }
        agenda.sort(Comparator.comparing(Actividad::getInicio));
        return agenda;
    }

    // Si el id es 0 agrega la actividad; si no, reemplaza la que tiene ese id.
    public static boolean guardarActividad(Actividad actividad) {
        List<Actividad> actividades = listarActividades();
        if (actividad.getId() == 0) {
            int mayor = 0;
            for (Actividad otra : actividades) {
                mayor = Math.max(mayor, otra.getId());
            }
            actividad.setId(mayor + 1);
            actividades.add(actividad);
        } else {
            for (int i = 0; i < actividades.size(); i++) {
                if (actividades.get(i).getId() == actividad.getId()) {
                    actividades.set(i, actividad);
                }
            }
        }
        return escribirActividades(actividades);
    }

    // Elimina la actividad y sus marcas de asistencia.
    public static boolean eliminarActividad(int id) {
        List<Actividad> actividades = listarActividades();
        for (int i = actividades.size() - 1; i >= 0; i--) {
            if (actividades.get(i).getId() == id) {
                actividades.remove(i);
            }
        }
        List<Asistencia> asistencias = listarAsistencias();
        quitarMarcasDeActividad(asistencias, id);
        return escribirAsistencias(asistencias) && escribirActividades(actividades);
    }

    private static boolean escribirActividades(List<Actividad> actividades) {
        List<String> lineas = new ArrayList<>();
        for (Actividad a : actividades) {
            lineas.add(a.getId() + "|" + a.getEventoId() + "|" + a.getNombre() + "|"
                    + a.getInicio() + "|" + a.getFin());
        }
        return escribirLineas(ACTIVIDADES, lineas);
    }

    private static void crearActividadesDeEjemplo() {
        LocalDateTime evento1 = hoyALasNueve().plusDays(7);
        LocalDateTime evento2 = hoyALasNueve().plusDays(14);
        LocalDateTime evento4 = hoyALasNueve().minusDays(10);
        List<Actividad> actividades = new ArrayList<>();
        actividades.add(new Actividad(1, 1, "Variables y tipos", evento1, evento1.plusMinutes(90)));
        actividades.add(new Actividad(2, 1, "Ciclos y funciones", evento1.plusMinutes(90), evento1.plusMinutes(180)));
        actividades.add(new Actividad(3, 2, "Charla central", evento2, evento2.plusHours(2)));
        // El evento 4 ya pasó y tiene cinco actividades de una hora.
        for (int i = 0; i < 5; i++) {
            actividades.add(new Actividad(4 + i, 4, "Sesión " + (i + 1), evento4.plusHours(i), evento4.plusHours(i + 1)));
        }
        escribirActividades(actividades);
    }

    // ---------- Inscripciones ----------

    private static List<Inscripcion> listarInscripciones() {
        if (!Files.exists(INSCRIPCIONES)) {
            crearInscripcionesDeEjemplo();
        }
        List<Inscripcion> inscripciones = new ArrayList<>();
        for (String linea : leerLineas(INSCRIPCIONES)) {
            try {
                String[] campos = separar(linea, 7);
                Inscripcion inscripcion = new Inscripcion(Integer.parseInt(campos[0]), Integer.parseInt(campos[1]),
                        campos[2], campos[3], campos[4], LocalDateTime.parse(campos[5]));
                if (!campos[6].isEmpty()) {
                    inscripcion.promover(LocalDateTime.parse(campos[6]));
                }
                inscripciones.add(inscripcion);
            } catch (RuntimeException e) {
                System.out.println("Línea ignorada en inscripciones.txt: " + linea);
            }
        }
        return inscripciones;
    }

    // Inscripciones de un evento, en orden de llegada.
    public static List<Inscripcion> listarInscripciones(int eventoId) {
        List<Inscripcion> delEvento = new ArrayList<>();
        for (Inscripcion inscripcion : listarInscripciones()) {
            if (inscripcion.getEventoId() == eventoId) {
                delEvento.add(inscripcion);
            }
        }
        return delEvento;
    }

    public static boolean agregarInscripcion(Inscripcion inscripcion) {
        List<Inscripcion> inscripciones = listarInscripciones();
        int mayor = 0;
        for (Inscripcion otra : inscripciones) {
            mayor = Math.max(mayor, otra.getId());
        }
        inscripcion.setId(mayor + 1);
        inscripciones.add(inscripcion);
        return escribirInscripciones(inscripciones);
    }

    // Elimina la inscripción y sus marcas de asistencia.
    public static boolean cancelarInscripcion(int id) {
        List<Inscripcion> inscripciones = listarInscripciones();
        for (int i = inscripciones.size() - 1; i >= 0; i--) {
            if (inscripciones.get(i).getId() == id) {
                inscripciones.remove(i);
            }
        }
        List<Asistencia> asistencias = listarAsistencias();
        for (int i = asistencias.size() - 1; i >= 0; i--) {
            if (asistencias.get(i).getInscripcionId() == id) {
                asistencias.remove(i);
            }
        }
        return escribirAsistencias(asistencias) && escribirInscripciones(inscripciones);
    }

    // Pasa a inscrita a la primera persona en espera. Devuelve null si no hay nadie.
    public static Inscripcion promoverPrimeroEnEspera(int eventoId) {
        List<Inscripcion> inscripciones = listarInscripciones();
        for (Inscripcion inscripcion : inscripciones) {
            if (inscripcion.getEventoId() == eventoId
                    && inscripcion.getEstado().equals(Inscripcion.EN_ESPERA)) {
                inscripcion.promover(LocalDateTime.now());
                return escribirInscripciones(inscripciones) ? inscripcion : null;
            }
        }
        return null;
    }

    // Si el evento tiene cupos libres (por ejemplo, porque se subió la capacidad),
    // pasa a inscritas a las primeras personas en espera. Devuelve las promovidas.
    public static List<Inscripcion> promoverSegunCupo(int eventoId, int capacidad) {
        List<Inscripcion> inscripciones = listarInscripciones();
        int inscritos = 0;
        for (Inscripcion inscripcion : inscripciones) {
            if (inscripcion.getEventoId() == eventoId
                    && inscripcion.getEstado().equals(Inscripcion.INSCRITO)) {
                inscritos++;
            }
        }
        List<Inscripcion> promovidas = new ArrayList<>();
        for (Inscripcion inscripcion : inscripciones) {
            if (inscritos >= capacidad) {
                break;
            }
            if (inscripcion.getEventoId() == eventoId
                    && inscripcion.getEstado().equals(Inscripcion.EN_ESPERA)) {
                inscripcion.promover(LocalDateTime.now());
                promovidas.add(inscripcion);
                inscritos++;
            }
        }
        if (!promovidas.isEmpty() && !escribirInscripciones(inscripciones)) {
            return new ArrayList<>();
        }
        return promovidas;
    }

    private static boolean escribirInscripciones(List<Inscripcion> inscripciones) {
        List<String> lineas = new ArrayList<>();
        for (Inscripcion i : inscripciones) {
            lineas.add(i.getId() + "|" + i.getEventoId() + "|" + i.getNombre() + "|"
                    + i.getDocumento() + "|" + i.getEstado() + "|" + i.getFechaRegistro() + "|"
                    + (i.getFechaPromocion() == null ? "" : i.getFechaPromocion()));
        }
        return escribirLineas(INSCRIPCIONES, lineas);
    }

    private static void crearInscripcionesDeEjemplo() {
        LocalDateTime ayer = hoyALasNueve().minusDays(1);
        LocalDateTime antes = hoyALasNueve().minusDays(15);
        List<Inscripcion> inscripciones = new ArrayList<>();
        // El evento 1 queda lleno y con dos personas en espera.
        inscripciones.add(new Inscripcion(1, 1, "Ana Gómez", "10010001", Inscripcion.INSCRITO, ayer));
        inscripciones.add(new Inscripcion(2, 1, "Luis Pérez", "10010002", Inscripcion.INSCRITO, ayer.plusMinutes(5)));
        inscripciones.add(new Inscripcion(3, 1, "Marta Ruiz", "10010003", Inscripcion.INSCRITO, ayer.plusMinutes(10)));
        inscripciones.add(new Inscripcion(4, 1, "Carlos Díaz", "10010004", Inscripcion.EN_ESPERA, ayer.plusMinutes(15)));
        inscripciones.add(new Inscripcion(5, 1, "Diana Mora", "10010005", Inscripcion.EN_ESPERA, ayer.plusMinutes(20)));
        inscripciones.add(new Inscripcion(6, 2, "Jorge Silva", "10010006", Inscripcion.INSCRITO, ayer));
        inscripciones.add(new Inscripcion(7, 2, "Paula Rojas", "10010007", Inscripcion.INSCRITO, ayer.plusMinutes(5)));
        inscripciones.add(new Inscripcion(8, 4, "Elena Castro", "10010008", Inscripcion.INSCRITO, antes));
        inscripciones.add(new Inscripcion(9, 4, "Andrés Vega", "10010009", Inscripcion.INSCRITO, antes.plusMinutes(5)));
        inscripciones.add(new Inscripcion(10, 4, "Sofía León", "10010010", Inscripcion.INSCRITO, antes.plusMinutes(10)));
        escribirInscripciones(inscripciones);
    }

    // ---------- Asistencias ----------

    private static List<Asistencia> listarAsistencias() {
        if (!Files.exists(ASISTENCIAS)) {
            crearAsistenciasDeEjemplo();
        }
        List<Asistencia> asistencias = new ArrayList<>();
        for (String linea : leerLineas(ASISTENCIAS)) {
            try {
                String[] campos = separar(linea, 2);
                asistencias.add(new Asistencia(Integer.parseInt(campos[0]), Integer.parseInt(campos[1])));
            } catch (RuntimeException e) {
                System.out.println("Línea ignorada en asistencias.txt: " + linea);
            }
        }
        return asistencias;
    }

    // Ids de las inscripciones que asistieron a una actividad.
    public static List<Integer> listarAsistentes(int actividadId) {
        List<Integer> asistentes = new ArrayList<>();
        for (Asistencia asistencia : listarAsistencias()) {
            if (asistencia.getActividadId() == actividadId) {
                asistentes.add(asistencia.getInscripcionId());
            }
        }
        return asistentes;
    }

    // Reemplaza las marcas de la actividad por las recibidas.
    public static boolean guardarAsistentes(int actividadId, List<Integer> inscripcionIds) {
        List<Asistencia> asistencias = listarAsistencias();
        quitarMarcasDeActividad(asistencias, actividadId);
        for (int inscripcionId : inscripcionIds) {
            asistencias.add(new Asistencia(actividadId, inscripcionId));
        }
        return escribirAsistencias(asistencias);
    }

    private static void quitarMarcasDeActividad(List<Asistencia> asistencias, int actividadId) {
        for (int i = asistencias.size() - 1; i >= 0; i--) {
            if (asistencias.get(i).getActividadId() == actividadId) {
                asistencias.remove(i);
            }
        }
    }

    private static boolean escribirAsistencias(List<Asistencia> asistencias) {
        List<String> lineas = new ArrayList<>();
        for (Asistencia a : asistencias) {
            lineas.add(a.getActividadId() + "|" + a.getInscripcionId());
        }
        return escribirLineas(ASISTENCIAS, lineas);
    }

    // En el evento 4 (actividades 4 a 8), Elena asiste a 5, Andrés a 4 y Sofía a 3.
    private static void crearAsistenciasDeEjemplo() {
        List<Asistencia> asistencias = new ArrayList<>();
        for (int actividadId = 4; actividadId <= 8; actividadId++) {
            asistencias.add(new Asistencia(actividadId, 8));
            if (actividadId <= 7) {
                asistencias.add(new Asistencia(actividadId, 9));
            }
            if (actividadId <= 6) {
                asistencias.add(new Asistencia(actividadId, 10));
            }
        }
        escribirAsistencias(asistencias);
    }

    // ---------- Utilidades ----------

    private static LocalDateTime hoyALasNueve() {
        return LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);
    }

    // Lanza una excepción si la línea no tiene la cantidad de campos esperada.
    private static String[] separar(String linea, int cantidadCampos) {
        String[] campos = linea.split("\\|", -1);
        if (campos.length != cantidadCampos) {
            throw new IllegalArgumentException("Cantidad de campos incorrecta");
        }
        return campos;
    }

    private static List<String> leerLineas(Path archivo) {
        List<String> lineas = new ArrayList<>();
        try {
            for (String linea : Files.readAllLines(archivo)) {
                if (!linea.isBlank()) {
                    lineas.add(linea);
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer " + archivo + ": " + e.getMessage());
        }
        return lineas;
    }

    private static boolean escribirLineas(Path archivo, List<String> lineas) {
        try {
            Files.createDirectories(CARPETA);
            Files.write(archivo, lineas);
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo escribir " + archivo + ": " + e.getMessage());
            return false;
        }
    }
}
