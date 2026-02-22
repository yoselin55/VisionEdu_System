package controller;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.Main;
import utils.IdiomaManager;
import utils.ThemeManager;

public class MenuPrincipalController {

    @FXML private Button btnGestionAlumnos;
    @FXML private Button btnControlAcceso;
    @FXML private Button btnHistorialAsistencia;
    @FXML private Button btnToggleTheme;
    @FXML private Button btnIdioma;
    @FXML private Button btnCerrarSesion;   // ← nuevo
    @FXML private Label  lblSubtitulo;      // ← nuevo

    @FXML
    public void initialize() {
        aplicarIdioma();
        if (btnToggleTheme != null) {
            agregarEfectoHover(btnToggleTheme);
            actualizarIconoTema();
        }
    }

    private void aplicarIdioma() {
        if (btnGestionAlumnos != null) {
            btnGestionAlumnos.setText(
                    IdiomaManager.t("menu.gestion").toUpperCase());
            agregarEfectoHover(btnGestionAlumnos);
        }
        if (btnControlAcceso != null) {
            btnControlAcceso.setText(
                    IdiomaManager.t("menu.acceso").toUpperCase());
            agregarEfectoHover(btnControlAcceso);
        }
        if (btnHistorialAsistencia != null) {
            btnHistorialAsistencia.setText(
                    IdiomaManager.t("menu.historial").toUpperCase());
            agregarEfectoHover(btnHistorialAsistencia);
        }
        if (lblSubtitulo != null) {
            lblSubtitulo.setText(IdiomaManager.t("menu.subtitulo"));
        }
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setText(IdiomaManager.t("menu.cerrar_sesion"));
            agregarEfectoHover(btnCerrarSesion);
        }
        if (btnIdioma != null) {
            String bandera = switch (IdiomaManager.getIdioma()) {
                case ESPANOL   -> "\uD83C\uDDEA\uD83C\uDDF8";
                case INGLES    -> "\uD83C\uDDFA\uD83C\uDDF8";
                case PORTUGUES -> "\uD83C\uDDE7\uD83C\uDDF7";
            };
            btnIdioma.setText(bandera + "  " + IdiomaManager.getNombreIdioma());
            agregarEfectoHover(btnIdioma);
        }
    }

    private void agregarEfectoHover(Button boton) {
        boton.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), boton);
            st.setToX(1.03); st.setToY(1.03); st.play();
        });
        boton.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), boton);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
    }

    @FXML public void handleGestionAlumnos(ActionEvent e)     { Main.mostrarGestionAlumnos(); }
    @FXML public void handleControlAcceso(ActionEvent e)       { Main.mostrarControlAcceso(); }
    @FXML public void handleHistorialAsistencia(ActionEvent e) { Main.mostrarHistorial(); }
    @FXML public void handleCerrarSesion(ActionEvent e)        { Main.mostrarLogin(); }

    @FXML
    public void handleToggleTheme(ActionEvent event) {
        ThemeManager.toggleTheme(btnToggleTheme.getScene());
        actualizarIconoTema();
    }

    private void actualizarIconoTema() {
        if (btnToggleTheme == null) return;
        if (ThemeManager.isDarkMode()) {
            btnToggleTheme.setText("\uD83C\uDF19");
            Tooltip.install(btnToggleTheme, new Tooltip("Modo Claro / Light Mode"));
        } else {
            btnToggleTheme.setText("\u2600");
            Tooltip.install(btnToggleTheme, new Tooltip("Modo Oscuro / Dark Mode"));
        }
    }

    @FXML
    public void handleCambiarIdioma(ActionEvent event) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Idioma / Language / Idioma");
        dialog.setResizable(false);

        VBox root = new VBox(14);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #1e2740;");
        root.setAlignment(Pos.CENTER);

        Label titulo = new Label("Seleccionar Idioma / Select Language");
        titulo.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e8eaf6;");

        Label lblActual = new Label("✔  " + IdiomaManager.getNombreIdioma());
        lblActual.setStyle(
                "-fx-text-fill: #66bb6a; -fx-font-size: 13px; -fx-font-weight: bold;");

        Button btnEs = crearBtnIdioma("\uD83C\uDDEA\uD83C\uDDF8  Español",   "#1565c0");
        Button btnEn = crearBtnIdioma("\uD83C\uDDFA\uD83C\uDDF8  English",   "#1b5e20");
        Button btnPt = crearBtnIdioma("\uD83C\uDDE7\uD83C\uDDF7  Português", "#4a148c");

        actualizarBordeActivo(btnEs, btnEn, btnPt);

        btnEs.setOnAction(e -> cambiarYCerrar(
                IdiomaManager.Idioma.ESPANOL,   dialog, lblActual, btnEs, btnEn, btnPt));
        btnEn.setOnAction(e -> cambiarYCerrar(
                IdiomaManager.Idioma.INGLES,    dialog, lblActual, btnEs, btnEn, btnPt));
        btnPt.setOnAction(e -> cambiarYCerrar(
                IdiomaManager.Idioma.PORTUGUES, dialog, lblActual, btnEs, btnEn, btnPt));

        Button btnCerrar = new Button("✕  Cerrar / Close");
        btnCerrar.setMaxWidth(Double.MAX_VALUE);
        btnCerrar.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #5c6bc0;" +
                        "-fx-border-width: 1.5; -fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-text-fill: #9fa8da; -fx-font-size: 13px; -fx-cursor: hand;" +
                        "-fx-pref-height: 38;");
        btnCerrar.setOnAction(e -> dialog.close());

        root.getChildren().addAll(
                titulo, new Separator(),
                btnEs, btnEn, btnPt,
                new Separator(), lblActual, btnCerrar
        );

        dialog.setScene(new Scene(root, 300, 370));
        dialog.showAndWait();
    }

    private void cambiarYCerrar(IdiomaManager.Idioma nuevoIdioma,
                                Stage dialog, Label lblActual,
                                Button btnEs, Button btnEn, Button btnPt) {
        IdiomaManager.setIdioma(nuevoIdioma);
        lblActual.setText("✔  " + IdiomaManager.getNombreIdioma());
        actualizarBordeActivo(btnEs, btnEn, btnPt);
        Platform.runLater(() -> {
            dialog.close();
            Main.mostrarMenuPrincipal();
        });
    }

    private Button crearBtnIdioma(String texto, String color) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(50);
        btn.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white;" +
                        "-fx-font-size: 15px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;");
        return btn;
    }

    private void actualizarBordeActivo(Button btnEs, Button btnEn, Button btnPt) {
        IdiomaManager.Idioma actual = IdiomaManager.getIdioma();
        String[] colores = {"#1565c0", "#1b5e20", "#4a148c"};
        Button[] botones = {btnEs, btnEn, btnPt};
        IdiomaManager.Idioma[] idiomas = {
                IdiomaManager.Idioma.ESPANOL,
                IdiomaManager.Idioma.INGLES,
                IdiomaManager.Idioma.PORTUGUES
        };
        for (int i = 0; i < botones.length; i++) {
            boolean activo = idiomas[i] == actual;
            botones[i].setStyle(
                    "-fx-background-color: " + colores[i] + ";" +
                            "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 10; -fx-cursor: hand; -fx-pref-height: 50;" +
                            (activo ? "-fx-border-color: white; -fx-border-width: 3;" +
                                    "-fx-border-radius: 10;" : ""));
        }
    }
}