/*
 * Autor: Samuel Chica Vélez
 * ID: 000279346
 */
package com.eventos;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada de la aplicación. También define el tema visual.
@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
public class EventosApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(EventosApplication.class, args);
    }
}
