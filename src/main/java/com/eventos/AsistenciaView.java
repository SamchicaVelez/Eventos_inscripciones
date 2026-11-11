/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Marca quiénes asistieron a cada actividad de un evento.
@Route(value = "asistencia", layout = MainLayout.class)
@PageTitle("Asistencia")
public class AsistenciaView extends VerticalLayout {

    private final ComboBox<Evento> evento = new ComboBox<>("Evento");
    private final ComboBox<Actividad> actividad = new ComboBox<>("Actividad");
    private final CheckboxGroup<Inscripcion> asistentes = new CheckboxGroup<>("Asistentes");

    public AsistenciaView() {
        evento.setItems(ArchivoDatos.listarEventos());
        evento.setItemLabelGenerator(Evento::getNombre);
        evento.addValueChangeListener(e -> cambiarEvento());

        actividad.setItemLabelGenerator(a -> a.getNombre() + " (" + a.getInicioTexto() + ")");
        actividad.addValueChangeListener(e -> cargarAsistentes());

        asistentes.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        asistentes.setItemLabelGenerator(i -> i.getNombre() + " (" + i.getDocumento() + ")");

        Button guardar = new Button("Guardar asistencia", e -> guardar());
        guardar.addThemeVariants(ButtonVariant.PRIMARY);

        add(evento, actividad, asistentes, guardar);
    }

    private void cambiarEvento() {
        if (evento.getValue() == null) {
            actividad.setItems(List.of());
        } else {
            actividad.setItems(ArchivoDatos.listarActividades(evento.getValue().getId()));
        }
        actividad.clear();
        cargarAsistentes();
    }

    // Muestra solo a los inscritos y marca a quienes ya tienen asistencia.
    private void cargarAsistentes() {
        asistentes.clear();
        if (actividad.getValue() == null) {
            asistentes.setItems(List.of());
            return;
        }
        List<Inscripcion> inscritos = new ArrayList<>();
        for (Inscripcion inscripcion : ArchivoDatos.listarInscripciones(evento.getValue().getId())) {
            if (inscripcion.getEstado().equals(Inscripcion.INSCRITO)) {
                inscritos.add(inscripcion);
            }
        }
        asistentes.setItems(inscritos);

        List<Integer> marcados = ArchivoDatos.listarAsistentes(actividad.getValue().getId());
        Set<Inscripcion> seleccion = new HashSet<>();
        for (Inscripcion inscripcion : inscritos) {
            if (marcados.contains(inscripcion.getId())) {
                seleccion.add(inscripcion);
            }
        }
        asistentes.setValue(seleccion);
    }

    private void guardar() {
        if (actividad.getValue() == null) {
            MainLayout.avisar("Primero elija un evento y una actividad.");
            return;
        }
        List<Integer> ids = new ArrayList<>();
        for (Inscripcion inscripcion : asistentes.getSelectedItems()) {
            ids.add(inscripcion.getId());
        }
        if (ArchivoDatos.guardarAsistentes(actividad.getValue().getId(), ids)) {
            MainLayout.avisar("Asistencia guardada.");
        } else {
            MainLayout.avisar("No se pudo guardar la asistencia.");
        }
    }
}
