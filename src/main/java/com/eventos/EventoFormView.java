/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDateTime;

// Formulario para crear un evento o, si la URL trae un id, para editarlo.
@Route(value = "evento", layout = MainLayout.class)
@PageTitle("Evento")
public class EventoFormView extends VerticalLayout implements HasUrlParameter<Integer> {

    private static final int CAPACIDAD_MAXIMA = 10000;

    private final H2 titulo = new H2("Nuevo evento");
    private final TextField nombre = new TextField("Nombre");
    private final ComboBox<String> categoria = new ComboBox<>("Categoría");
    private final DateTimePicker fechaHora = new DateTimePicker("Fecha y hora");
    private final TextField lugar = new TextField("Lugar");
    private final IntegerField capacidad = new IntegerField("Capacidad");
    private final TextArea descripcion = new TextArea("Descripción");

    // Es null mientras se crea un evento nuevo.
    private Evento eventoEditado;

    public EventoFormView() {
        categoria.setItems(Evento.CATEGORIAS);
        capacidad.setMin(1);
        capacidad.setMax(CAPACIDAD_MAXIMA);

        // FormLayout pasa a una sola columna en pantallas angostas.
        FormLayout formulario = new FormLayout(nombre, categoria, fechaHora, lugar, capacidad, descripcion);

        Button guardar = new Button("Guardar", e -> guardar());
        guardar.addThemeVariants(ButtonVariant.PRIMARY);
        Button cancelar = new Button("Cancelar", e -> UI.getCurrent().navigate(EventosView.class));

        add(titulo, formulario, new HorizontalLayout(guardar, cancelar));
    }

    @Override
    public void setParameter(BeforeEvent evento, @OptionalParameter Integer id) {
        if (id == null) {
            return;
        }
        eventoEditado = ArchivoDatos.buscarEvento(id);
        if (eventoEditado == null) {
            MainLayout.avisar("El evento no existe.");
            evento.forwardTo(EventosView.class);
            return;
        }
        titulo.setText("Editar evento");
        nombre.setValue(eventoEditado.getNombre());
        categoria.setValue(eventoEditado.getCategoria());
        fechaHora.setValue(eventoEditado.getFechaHora());
        lugar.setValue(eventoEditado.getLugar());
        capacidad.setValue(eventoEditado.getCapacidad());
        descripcion.setValue(eventoEditado.getDescripcion());
    }

    private void guardar() {
        String error = validar();
        if (error != null) {
            MainLayout.avisar(error);
            return;
        }
        int id = eventoEditado == null ? 0 : eventoEditado.getId();
        // Los saltos de línea dañarían el formato del archivo.
        String textoDescripcion = descripcion.getValue().trim().replaceAll("\\s*\\R\\s*", " ");
        Evento evento = new Evento(id, nombre.getValue().trim(), categoria.getValue(), fechaHora.getValue(),
                lugar.getValue().trim(), capacidad.getValue(), textoDescripcion);
        if (ArchivoDatos.guardarEvento(evento)) {
            String mensaje = "Evento guardado.";
            // Si se amplió la capacidad, la lista de espera avanza sola.
            if (eventoEditado != null) {
                int promovidas = ArchivoDatos.promoverSegunCupo(id, evento.getCapacidad()).size();
                if (promovidas > 0) {
                    mensaje += " " + promovidas + " persona(s) pasaron de la lista de espera a inscritas.";
                }
            }
            MainLayout.avisar(mensaje);
            UI.getCurrent().navigate(EventosView.class);
        } else {
            MainLayout.avisar("No se pudo guardar el evento.");
        }
    }

    // Devuelve el mensaje del primer error, o null si todo está bien.
    private String validar() {
        if (nombre.getValue().isBlank()) {
            return "El nombre es obligatorio.";
        }
        if (categoria.getValue() == null) {
            return "La categoría es obligatoria.";
        }
        if (fechaHora.getValue() == null) {
            return "La fecha y hora son obligatorias y deben tener un formato válido.";
        }
        if (eventoEditado == null && fechaHora.getValue().isBefore(LocalDateTime.now())) {
            return "La fecha y hora de un evento nuevo no pueden estar en el pasado.";
        }
        if (lugar.getValue().isBlank()) {
            return "El lugar es obligatorio.";
        }
        if (capacidad.getValue() == null) {
            return "La capacidad es obligatoria y debe ser un número entero.";
        }
        if (capacidad.getValue() < 1 || capacidad.getValue() > CAPACIDAD_MAXIMA) {
            return "La capacidad debe estar entre 1 y " + CAPACIDAD_MAXIMA + ".";
        }
        if (eventoEditado != null && capacidad.getValue() < eventoEditado.getInscritos()) {
            return "La capacidad no puede ser menor que los " + eventoEditado.getInscritos() + " inscritos actuales.";
        }
        if (eventoEditado != null) {
            for (Actividad actividad : ArchivoDatos.listarActividades(eventoEditado.getId())) {
                if (actividad.getInicio().isBefore(fechaHora.getValue())) {
                    return "La fecha no puede ser posterior a la actividad \"" + actividad.getNombre() + "\".";
                }
            }
        }
        if (descripcion.getValue().isBlank()) {
            return "La descripción es obligatoria.";
        }
        if (nombre.getValue().contains("|") || lugar.getValue().contains("|")
                || descripcion.getValue().contains("|")) {
            return "Los textos no pueden contener el carácter |.";
        }
        return null;
    }
}
