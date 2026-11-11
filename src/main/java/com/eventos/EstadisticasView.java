/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

// Ocupación y asistencia de cada evento.
@Route(value = "estadisticas", layout = MainLayout.class)
@PageTitle("Estadísticas")
public class EstadisticasView extends VerticalLayout {

    public EstadisticasView() {
        setSizeFull();

        Grid<Evento> grid = new Grid<>();
        grid.setSizeFull();
        grid.addColumn(Evento::getNombre).setHeader("Evento").setAutoWidth(true);
        grid.addComponentColumn(e -> crearIndicador(e.getOcupacion())).setHeader("Ocupación").setWidth("130px").setFlexGrow(0);
        grid.addComponentColumn(e -> crearIndicador(e.getAsistencia())).setHeader("Asistencia").setWidth("130px").setFlexGrow(0);
        grid.setItems(ArchivoDatos.listarEventos());
        add(grid);
    }

    // Muestra una fracción de 0 a 1 como porcentaje y como barra.
    private VerticalLayout crearIndicador(double fraccion) {
        ProgressBar barra = new ProgressBar();
        barra.setValue(fraccion);
        VerticalLayout indicador = new VerticalLayout(new Span(Math.round(fraccion * 100) + " %"), barra);
        indicador.setPadding(false);
        indicador.setSpacing(false);
        return indicador;
    }
}
