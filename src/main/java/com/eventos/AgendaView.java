/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;

// Agenda de un evento: actividades con hora de inicio y fin.
@Route(value = "agenda", layout = MainLayout.class)
@PageTitle("Agenda")
public class AgendaView extends VerticalLayout {

    private final ComboBox<Evento> evento = new ComboBox<>("Evento");
    private final TextField nombre = new TextField("Actividad");
    private final DateTimePicker inicio = new DateTimePicker("Inicio");
    private final DateTimePicker fin = new DateTimePicker("Fin");
    private final Grid<Actividad> grid = new Grid<>();

    // Es null mientras se crea una actividad nueva.
    private Actividad actividadEditada;

    public AgendaView() {
        setSizeFull();

        evento.setItems(ArchivoDatos.listarEventos());
        evento.setItemLabelGenerator(Evento::getNombre);
        evento.addValueChangeListener(e -> {
            limpiarFormulario();
            actualizarGrid();
        });

        Button guardar = new Button("Guardar actividad", e -> guardar());
        guardar.addThemeVariants(ButtonVariant.PRIMARY);
        Button limpiar = new Button("Limpiar", e -> limpiarFormulario());

        grid.setSizeFull();
        grid.addColumn(Actividad::getNombre).setHeader("Actividad").setAutoWidth(true);
        grid.addColumn(Actividad::getInicioTexto).setHeader("Inicio").setAutoWidth(true);
        grid.addColumn(Actividad::getFinTexto).setHeader("Fin").setAutoWidth(true);
        grid.addComponentColumn(this::crearAcciones).setHeader("Acciones").setAutoWidth(true);

        add(evento, new FormLayout(nombre, inicio, fin), new HorizontalLayout(guardar, limpiar), grid);
    }

    private HorizontalLayout crearAcciones(Actividad actividad) {
        Button editar = new Button("Editar", e -> cargarEnFormulario(actividad));
        Button eliminar = new Button("Eliminar", e -> MainLayout.confirmar(
                "¿Eliminar la actividad \"" + actividad.getNombre() + "\"?", () -> eliminar(actividad)));
        return new HorizontalLayout(editar, eliminar);
    }

    private void cargarEnFormulario(Actividad actividad) {
        actividadEditada = actividad;
        nombre.setValue(actividad.getNombre());
        inicio.setValue(actividad.getInicio());
        fin.setValue(actividad.getFin());
    }

    private void limpiarFormulario() {
        actividadEditada = null;
        nombre.clear();
        inicio.clear();
        fin.clear();
    }

    private void eliminar(Actividad actividad) {
        if (ArchivoDatos.eliminarActividad(actividad.getId())) {
            MainLayout.avisar("Actividad eliminada.");
        } else {
            MainLayout.avisar("No se pudo eliminar la actividad.");
        }
        limpiarFormulario();
        actualizarGrid();
    }

    private void guardar() {
        String error = validar();
        if (error != null) {
            MainLayout.avisar(error);
            return;
        }
        int id = actividadEditada == null ? 0 : actividadEditada.getId();
        Actividad actividad = new Actividad(id, evento.getValue().getId(), nombre.getValue().trim(),
                inicio.getValue(), fin.getValue());
        if (ArchivoDatos.guardarActividad(actividad)) {
            MainLayout.avisar("Actividad guardada.");
            limpiarFormulario();
            actualizarGrid();
        } else {
            MainLayout.avisar("No se pudo guardar la actividad.");
        }
    }

    // Devuelve el mensaje del primer error, o null si todo está bien.
    private String validar() {
        if (evento.getValue() == null) {
            return "Primero elija un evento.";
        }
        if (nombre.getValue().isBlank()) {
            return "El nombre de la actividad es obligatorio.";
        }
        if (nombre.getValue().contains("|")) {
            return "Los textos no pueden contener el carácter |.";
        }
        if (inicio.getValue() == null || fin.getValue() == null) {
            return "El inicio y el fin son obligatorios y deben tener un formato válido.";
        }
        if (!fin.getValue().isAfter(inicio.getValue())) {
            return "El fin debe ser posterior al inicio.";
        }
        if (inicio.getValue().isBefore(evento.getValue().getFechaHora())) {
            return "La actividad no puede empezar antes del evento.";
        }
        for (Actividad otra : ArchivoDatos.listarActividades(evento.getValue().getId())) {
            boolean esLaMisma = actividadEditada != null && otra.getId() == actividadEditada.getId();
            // Se cruzan si cada una empieza antes de que la otra termine.
            boolean seCruzan = inicio.getValue().isBefore(otra.getFin())
                    && otra.getInicio().isBefore(fin.getValue());
            if (!esLaMisma && seCruzan) {
                return "El horario se cruza con la actividad \"" + otra.getNombre() + "\".";
            }
        }
        return null;
    }

    private void actualizarGrid() {
        if (evento.getValue() == null) {
            grid.setItems(List.of());
        } else {
            grid.setItems(ArchivoDatos.listarActividades(evento.getValue().getId()));
        }
    }
}
