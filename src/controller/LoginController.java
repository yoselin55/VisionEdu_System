package controller;

import main.Main;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import utils.IdiomaManager;
public class LoginController {

    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button        btnLogin;
    @FXML private Label         lblSubtituloLogin;  // ← nuevo

    @FXML
    public void initialize() {
        aplicarIdioma();

        if (btnLogin   != null) agregarEfectoHover(btnLogin);
        if (txtUsuario != null) agregarEfectoFocus(txtUsuario);
        if (txtPassword!= null) agregarEfectoFocus(txtPassword);
    }

    private void aplicarIdioma() {
        if (lblSubtituloLogin != null)
            lblSubtituloLogin.setText(IdiomaManager.t("login.subtitulo"));
        if (btnLogin != null)
            btnLogin.setText(IdiomaManager.t("login.boton") + " \uD83D\uDE80");
        if (txtUsuario != null)
            txtUsuario.setPromptText(IdiomaManager.t("login.usuario"));
        if (txtPassword != null)
            txtPassword.setPromptText(IdiomaManager.t("login.password"));
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

    private void agregarEfectoFocus(javafx.scene.control.Control campo) {
        campo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal)
                campo.setStyle(
                        "-fx-border-color: #667eea; -fx-border-width: 2; -fx-border-radius: 10;");
            else
                campo.setStyle("-fx-border-color: transparent;");
        });
    }

    @FXML
    private void handleLogin() {
        String usuario  = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarAlerta(
                    IdiomaManager.t("login.alerta_campos_titulo"),
                    IdiomaManager.t("login.alerta_campos_msg"),
                    Alert.AlertType.WARNING);
            return;
        }

        if (usuario.equals("admin") && password.equals("12345678")) {
            TranslateTransition tt = new TranslateTransition(
                    Duration.millis(300), btnLogin);
            tt.setByY(-5);
            tt.setAutoReverse(true);
            tt.setCycleCount(2);
            tt.setOnFinished(e -> Main.mostrarMenuPrincipal());
            tt.play();
        } else {
            TranslateTransition shake = new TranslateTransition(
                    Duration.millis(100), btnLogin);
            shake.setByX(10);
            shake.setCycleCount(4);
            shake.setAutoReverse(true);
            shake.play();
            mostrarAlerta(
                    IdiomaManager.t("alerta.error"),
                    IdiomaManager.t("login.alerta_error_msg"),
                    Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}