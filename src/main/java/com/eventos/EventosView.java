/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;

// Lista de eventos con búsqueda, filtros y acciones de editar y eliminar.
@Route(value = "", layout = MainLayout.class)
@PageTitle("Eventos")
public class EventosView extends VerticalLayout {

    private final Grid<Evento> grid = new Grid<>();
    private final TextField buscar = new TextField("Buscar");
    private final ComboBox<String> categoria = new ComboBox<>("Categoría");
    private final DatePicker fecha = new DatePicker("Fecha");

    public EventosView() {
        setSizeFull();

        buscar.setPlaceholder("Nombre del evento");
        buscar.setClearButtonVisible(true);
        buscar.setValueChangeMode(ValueChangeMode.EAGER);
        buscar.addValueChangeListener(e -> actualizarGrid());

        categoria.setItems(Evento.CATEGORIAS);
        categoria.setClearButtonVisible(true);
        categoria.addValueChangeListener(e -> actualizarGrid());

        fecha.setClearButtonVisible(true);
        fecha.addValueChangeListener(e -> actualizarGrid());

        // FlexLayout permite que los filtros bajen de línea en el celular.
        FlexLayout filtros = new FlexLayout(buscar, categoria, fecha);
        filtros.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        filtros.getStyle().set("gap", "1em");

        Button nuevo = new Button("Nuevo evento", e -> UI.getCurrent().navigate(EventoFormView.class));

        configurarGrid();
        add(filtros, nuevo, grid);
        actualizarGrid();
    }

    private void configurarGrid() {
        grid.setSizeFull();
        grid.addColumn(e -> e.getNombre() + " (" + e.getCategoria() + ")").setHeader("Evento").setAutoWidth(true);
        grid.addColumn(Evento::getFechaTexto).setHeader("Fecha").setAutoWidth(true);
        grid.addColumn(Evento::getEstado).setHeader("Estado").setAutoWidth(true);
        grid.addComponentColumn(this::crearCupo).setHeader("Cupo").setWidth("130px").setFlexGrow(0);
        grid.addComponentColumn(this::crearAcciones).setHeader("Acciones").setAutoWidth(true);

        // Al hacer clic en una fila se ven los demás datos del evento.
        grid.setItemDetailsRenderer(new ComponentRenderer<>(e -> new Span(
                "Lugar: " + e.getLugar() + ". Capacidad: " + e.getCapacidad() + ". " + e.getDescripcion())));
    }

    // Muestra inscritos sobre capacidad y una barra con el cupo ocupado.
    private VerticalLayout crearCupo(Evento evento) {
        ProgressBar barra = new ProgressBar();
        barra.setValue(evento.getOcupacion());
        VerticalLayout cupo = new VerticalLayout(new Span(evento.getInscritos() + " / " + evento.getCapacidad()), barra);
        cupo.setPadding(false);
        cupo.setSpacing(false);
        return cupo;
    }

    private HorizontalLayout crearAcciones(Evento evento) {
        Button editar = new Button("Editar",
                e -> UI.getCurrent().navigate(EventoFormView.class, evento.getId()));
        Button eliminar = new Button("Eliminar", e -> MainLayout.confirmar(
                "¿Eliminar el evento \"" + evento.getNombre() + "\"?", () -> eliminar(evento)));
        return new HorizontalLayout(editar, eliminar);
    }

    private void eliminar(Evento evento) {
        if (ArchivoDatos.eliminarEvento(evento.getId())) {
            MainLayout.avisar("Evento eliminado.");
        } else {
            MainLayout.avisar("No se pudo eliminar el evento.");
        }
        actualizarGrid();
    }

    // Aplica la búsqueda y los filtros activos.
    private void actualizarGrid() {
        String texto = buscar.getValue().trim().toLowerCase();
        List<Evento> resultado = new ArrayList<>();
        for (Evento evento : ArchivoDatos.listarEventos()) {
            boolean coincideNombre = evento.getNombre().toLowerCase().contains(texto);
            boolean coincideCategoria = categoria.getValue() == null
                    || categoria.getValue().equals(evento.getCategoria());
            boolean coincideFecha = fecha.getValue() == null
                    || fecha.getValue().equals(evento.getFechaHora().toLocalDate());
            if (coincideNombre && coincideCategoria && coincideFecha) {
                resultado.add(evento);
            }
        }
        grid.setItems(resultado);
    }
}
