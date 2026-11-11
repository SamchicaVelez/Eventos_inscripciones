/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import java.time.LocalDateTime;

// Una actividad de la agenda de un evento.
public class Actividad {

    private int id;
    private int eventoId;
    private String nombre;
    private LocalDateTime inicio;
    private LocalDateTime fin;

    public Actividad(int id, int eventoId, String nombre, LocalDateTime inicio, LocalDateTime fin) {
        this.id = id;
        this.eventoId = eventoId;
        this.nombre = nombre;
        this.inicio = inicio;
        this.fin = fin;
    }

    public String getInicioTexto() {
        return inicio.format(Evento.FORMATO_FECHA);
    }

    public String getFinTexto() {
        return fin.format(Evento.FORMATO_FECHA);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventoId() {
        return eventoId;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }
}
