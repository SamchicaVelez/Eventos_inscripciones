/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Representa un taller o una conferencia.
public class Evento {

    public static final String ABIERTO = "Abierto";
    public static final String LLENO = "Lleno";
    public static final String FINALIZADO = "Finalizado";
    public static final List<String> CATEGORIAS = List.of("Taller", "Conferencia");
    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private String nombre;
    private String categoria;
    private LocalDateTime fechaHora;
    private String lugar;
    private int capacidad;
    private String descripcion;

    public Evento(int id, String nombre, String categoria, LocalDateTime fechaHora,
            String lugar, int capacidad, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.fechaHora = fechaHora;
        this.lugar = lugar;
        this.capacidad = capacidad;
        this.descripcion = descripcion;
    }

    // El estado no se guarda: se calcula cada vez que se consulta.
    public String getEstado() {
        // Sin agenda, el evento termina en su fecha; con agenda, al acabar la última actividad.
        List<Actividad> agenda = ArchivoDatos.listarActividades(id);
        LocalDateTime fin = agenda.isEmpty() ? fechaHora : agenda.get(agenda.size() - 1).getFin();
        if (LocalDateTime.now().isAfter(fin)) {
            return FINALIZADO;
        }
        if (getInscritos() >= capacidad) {
            return LLENO;
        }
        return ABIERTO;
    }

    public int getInscritos() {
        int inscritos = 0;
        for (Inscripcion inscripcion : ArchivoDatos.listarInscripciones(id)) {
            if (inscripcion.getEstado().equals(Inscripcion.INSCRITO)) {
                inscritos++;
            }
        }
        return inscritos;
    }

    // Fracción del cupo ocupada, de 0 a 1.
    public double getOcupacion() {
        if (capacidad <= 0) {
            return 0;
        }
        return Math.min(1.0, (double) getInscritos() / capacidad);
    }

    // Fracción de asistencia: marcas / (inscritos x actividades), de 0 a 1.
    public double getAsistencia() {
        List<Actividad> agenda = ArchivoDatos.listarActividades(id);
        int posibles = getInscritos() * agenda.size();
        if (posibles == 0) {
            return 0;
        }
        int marcas = 0;
        for (Actividad actividad : agenda) {
            marcas += ArchivoDatos.listarAsistentes(actividad.getId()).size();
        }
        return Math.min(1.0, (double) marcas / posibles);
    }

    public String getFechaTexto() {
        return fechaHora.format(FORMATO_FECHA);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getLugar() {
        return lugar;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
