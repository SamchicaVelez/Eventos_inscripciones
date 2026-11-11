/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

// Plantilla común: barra superior y menú lateral de todas las vistas.
public class MainLayout extends AppLayout {

    public MainLayout() {
        H1 titulo = new H1("Eventos e inscripciones");
        titulo.getStyle().set("font-size", "1.1em").set("margin", "0");
        addToNavbar(new DrawerToggle(), titulo);

        SideNav menu = new SideNav();
        menu.addItem(new SideNavItem("Eventos", EventosView.class));
        menu.addItem(new SideNavItem("Nuevo evento", EventoFormView.class));
        menu.addItem(new SideNavItem("Agenda", AgendaView.class));
        menu.addItem(new SideNavItem("Inscripciones", InscripcionesView.class));
        menu.addItem(new SideNavItem("Asistencia", AsistenciaView.class));
        menu.addItem(new SideNavItem("Estadísticas", EstadisticasView.class));
        menu.addItem(new SideNavItem("Certificados", CertificadosView.class));
        addToDrawer(menu);
    }

    // Muestra un mensaje al usuario.
    public static void avisar(String mensaje) {
        Notification.show(mensaje, 4000, Notification.Position.MIDDLE);
    }

    // Pide confirmación y ejecuta la acción solo si el usuario acepta.
    public static void confirmar(String mensaje, Runnable accion) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Confirmar");
        dialogo.add(new Span(mensaje));
        Button si = new Button("Sí", e -> {
            dialogo.close();
            accion.run();
        });
        Button no = new Button("No", e -> dialogo.close());
        dialogo.getFooter().add(no, si);
        dialogo.open();
    }
}
