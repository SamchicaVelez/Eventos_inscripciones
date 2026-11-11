/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AttachmentType;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Asistencia de cada inscrito y descarga de su certificado.
@Route(value = "certificados", layout = MainLayout.class)
@PageTitle("Certificados")
public class CertificadosView extends VerticalLayout {

    private static final int PORCENTAJE_MINIMO = 80;

    private final ComboBox<Evento> evento = new ComboBox<>("Evento");
    private final Grid<Inscripcion> grid = new Grid<>();

    public CertificadosView() {
        setSizeFull();

        evento.setItems(ArchivoDatos.listarEventos());
        evento.setItemLabelGenerator(Evento::getNombre);
        evento.addValueChangeListener(e -> actualizarGrid());

        grid.setSizeFull();
        grid.addColumn(Inscripcion::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(i -> porcentaje(i) + " %").setHeader("Asistencia").setAutoWidth(true);
        grid.addColumn(i -> textoHoras(i)).setHeader("Horas").setAutoWidth(true);
        grid.addComponentColumn(this::crearDescarga).setHeader("Certificado").setAutoWidth(true);

        add(evento, grid);
    }

    private void actualizarGrid() {
        List<Inscripcion> inscritos = new ArrayList<>();
        if (evento.getValue() != null) {
            for (Inscripcion inscripcion : ArchivoDatos.listarInscripciones(evento.getValue().getId())) {
                if (inscripcion.getEstado().equals(Inscripcion.INSCRITO)) {
                    inscritos.add(inscripcion);
                }
            }
        }
        grid.setItems(inscritos);
    }

    // Actividades del evento a las que asistió la persona.
    private List<Actividad> actividadesAsistidas(Inscripcion inscripcion) {
        List<Actividad> asistidas = new ArrayList<>();
        for (Actividad actividad : ArchivoDatos.listarActividades(inscripcion.getEventoId())) {
            if (ArchivoDatos.listarAsistentes(actividad.getId()).contains(inscripcion.getId())) {
                asistidas.add(actividad);
            }
        }
        return asistidas;
    }

    // Devuelve 0 si el evento no tiene agenda.
    private int porcentaje(Inscripcion inscripcion) {
        int total = ArchivoDatos.listarActividades(inscripcion.getEventoId()).size();
        if (total == 0) {
            return 0;
        }
        return actividadesAsistidas(inscripcion).size() * 100 / total;
    }

    private String textoHoras(Inscripcion inscripcion) {
        long minutos = 0;
        for (Actividad actividad : actividadesAsistidas(inscripcion)) {
            minutos += Duration.between(actividad.getInicio(), actividad.getFin()).toMinutes();
        }
        return String.format("%.1f", minutos / 60.0);
    }

    // Enlace de descarga si cumple el mínimo; si no, un texto.
    private Component crearDescarga(Inscripcion inscripcion) {
        if (porcentaje(inscripcion) < PORCENTAJE_MINIMO) {
            return new Span("No disponible");
        }
        Evento elegido = evento.getValue();
        DownloadHandler descarga = DownloadHandler.fromInputStream(solicitud -> {
            byte[] contenido = crearCertificado(inscripcion, elegido).getBytes(StandardCharsets.UTF_8);
            return new DownloadResponse(new ByteArrayInputStream(contenido),
                    "certificado-" + inscripcion.getId() + ".html", "text/html;charset=UTF-8", contenido.length);
        });
        return new Anchor(descarga, AttachmentType.DOWNLOAD, "Descargar");
    }

    // Documento HTML listo para imprimir desde el navegador.
    private String crearCertificado(Inscripcion inscripcion, Evento elegido) {
        String fecha = elegido.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                <meta charset="UTF-8">
                <title>Certificado de asistencia</title>
                <style>
                body { font-family: serif; text-align: center; margin: 4em; }
                h1 { font-size: 2.5em; }
                .nombre { font-size: 2em; font-weight: bold; }
                </style>
                </head>
                <body>
                <h1>Certificado de asistencia</h1>
                <p>Se certifica que</p>
                <p class="nombre">%s</p>
                <p>asistió al evento <strong>%s</strong>,</p>
                <p>realizado el %s, con un total de %s horas.</p>
                </body>
                </html>
                """.formatted(escapar(inscripcion.getNombre()), escapar(elegido.getNombre()),
                fecha, textoHoras(inscripcion));
    }

    // Evita que un texto con caracteres especiales altere el HTML.
    private String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
