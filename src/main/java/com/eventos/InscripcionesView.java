/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDateTime;
import java.util.List;

// Inscripción de personas a un evento, con cupo y lista de espera.
@Route(value = "inscripciones", layout = MainLayout.class)
@PageTitle("Inscripciones")
public class InscripcionesView extends VerticalLayout {

    private final ComboBox<Evento> evento = new ComboBox<>("Evento");
    private final TextField nombre = new TextField("Nombre");
    private final TextField documento = new TextField("Documento");
    private final Span resumen = new Span();
    private final Grid<Inscripcion> grid = new Grid<>();

    public InscripcionesView() {
        setSizeFull();

        evento.setItems(ArchivoDatos.listarEventos());
        evento.setItemLabelGenerator(Evento::getNombre);
        evento.addValueChangeListener(e -> actualizar());

        Button inscribir = new Button("Inscribir", e -> inscribir());
        inscribir.addThemeVariants(ButtonVariant.PRIMARY);

        grid.setSizeFull();
        grid.addColumn(Inscripcion::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Inscripcion::getDocumento).setHeader("Documento").setAutoWidth(true);
        grid.addColumn(Inscripcion::getEstadoTexto).setHeader("Estado").setAutoWidth(true);
        grid.addComponentColumn(this::crearCancelar).setHeader("Acción").setAutoWidth(true);

        add(evento, new FormLayout(nombre, documento), inscribir, resumen, grid);
    }

    private Button crearCancelar(Inscripcion inscripcion) {
        return new Button("Cancelar", e -> {
            if (evento.getValue().getEstado().equals(Evento.FINALIZADO)) {
                MainLayout.avisar("No se puede cancelar en un evento finalizado.");
                return;
            }
            MainLayout.confirmar("¿Cancelar la inscripción de " + inscripcion.getNombre() + "?",
                    () -> cancelar(inscripcion));
        });
    }

    private void cancelar(Inscripcion inscripcion) {
        if (!ArchivoDatos.cancelarInscripcion(inscripcion.getId())) {
            MainLayout.avisar("No se pudo cancelar la inscripción.");
            actualizar();
            return;
        }
        String mensaje = "Inscripción cancelada.";
        // Solo se libera un cupo si la persona estaba inscrita.
        if (inscripcion.getEstado().equals(Inscripcion.INSCRITO)) {
            Inscripcion promovida = ArchivoDatos.promoverPrimeroEnEspera(inscripcion.getEventoId());
            if (promovida != null) {
                mensaje += " " + promovida.getNombre() + " pasó de la lista de espera a inscrito.";
            }
        }
        MainLayout.avisar(mensaje);
        actualizar();
    }

    private void inscribir() {
        String error = validar();
        if (error != null) {
            MainLayout.avisar(error);
            return;
        }
        Evento elegido = evento.getValue();
        // Si no hay cupo, la persona pasa a la lista de espera.
        String estado = elegido.getInscritos() < elegido.getCapacidad()
                ? Inscripcion.INSCRITO : Inscripcion.EN_ESPERA;
        Inscripcion inscripcion = new Inscripcion(0, elegido.getId(), nombre.getValue().trim(),
                documento.getValue().trim(), estado, LocalDateTime.now());
        if (!ArchivoDatos.agregarInscripcion(inscripcion)) {
            MainLayout.avisar("No se pudo guardar la inscripción.");
        } else if (estado.equals(Inscripcion.INSCRITO)) {
            MainLayout.avisar("Persona inscrita.");
        } else {
            MainLayout.avisar("El evento está lleno: la persona quedó en lista de espera.");
        }
        nombre.clear();
        documento.clear();
        actualizar();
    }

    // Devuelve el mensaje del primer error, o null si todo está bien.
    private String validar() {
        if (evento.getValue() == null) {
            return "Primero elija un evento.";
        }
        if (evento.getValue().getEstado().equals(Evento.FINALIZADO)) {
            return "El evento ya finalizó: no admite inscripciones.";
        }
        if (nombre.getValue().isBlank()) {
            return "El nombre es obligatorio.";
        }
        if (nombre.getValue().contains("|")) {
            return "Los textos no pueden contener el carácter |.";
        }
        String texto = documento.getValue().trim();
        if (!texto.matches("[0-9]{5,15}")) {
            return "El documento debe tener entre 5 y 15 dígitos.";
        }
        for (Inscripcion otra : ArchivoDatos.listarInscripciones(evento.getValue().getId())) {
            if (otra.getDocumento().equals(texto)) {
                return "Esa persona ya está registrada en este evento.";
            }
        }
        return null;
    }

    private void actualizar() {
        Evento elegido = evento.getValue();
        if (elegido == null) {
            grid.setItems(List.of());
            resumen.setText("");
            return;
        }
        List<Inscripcion> inscripciones = ArchivoDatos.listarInscripciones(elegido.getId());
        grid.setItems(inscripciones);
        int inscritos = elegido.getInscritos();
        resumen.setText("Estado: " + elegido.getEstado() + ". Inscritos: " + inscritos + " de "
                + elegido.getCapacidad() + ". En espera: " + (inscripciones.size() - inscritos) + ".");
    }
}
