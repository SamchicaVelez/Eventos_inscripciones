/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

// Indica que una persona inscrita asistió a una actividad.
public class Asistencia {

    private int actividadId;
    private int inscripcionId;

    public Asistencia(int actividadId, int inscripcionId) {
        this.actividadId = actividadId;
        this.inscripcionId = inscripcionId;
    }

    public int getActividadId() {
        return actividadId;
    }

    public int getInscripcionId() {
        return inscripcionId;
    }
}
