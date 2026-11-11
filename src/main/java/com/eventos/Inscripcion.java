/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import java.time.LocalDateTime;

// El registro de una persona en un evento.
public class Inscripcion {

    public static final String INSCRITO = "Inscrito";
    public static final String EN_ESPERA = "En espera";

    private int id;
    private int eventoId;
    private String nombre;
    private String documento;
    private String estado;
    private LocalDateTime fechaRegistro;
    // Es null si la persona no pasó por la lista de espera.
    private LocalDateTime fechaPromocion;

    public Inscripcion(int id, int eventoId, String nombre, String documento,
            String estado, LocalDateTime fechaRegistro) {
        this.id = id;
        this.eventoId = eventoId;
        this.nombre = nombre;
        this.documento = documento;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    // Pasa a la persona de la lista de espera a inscrita y registra cuándo.
    public void promover(LocalDateTime momento) {
        estado = INSCRITO;
        fechaPromocion = momento;
    }

    public String getEstadoTexto() {
        if (fechaPromocion == null) {
            return estado;
        }
        return estado + " (promovido " + fechaPromocion.format(Evento.FORMATO_FECHA) + ")";
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

    public String getDocumento() {
        return documento;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getFechaPromocion() {
        return fechaPromocion;
    }
}
