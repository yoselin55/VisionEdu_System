package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.IdiomaManager;
import utils.ThemeManager;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("VisionEdu - Sistema Biométrico");
        primaryStage.setMaximized(true);
        IdiomaManager.cargarIdiomaSalvado();
        mostrarLogin();
        primaryStage.show();
    }

    private static void cambiarEscena(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
            Parent root = loader.load();
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            ThemeManager.init(scene);
            primaryStage.setMaximized(true);
            root.requestFocus();
        } catch (Exception e) {
            System.err.println("❌ Error al cargar: " + fxml);
            e.printStackTrace();
        }
    }

    public static void mostrarLogin()          { cambiarEscena("/view/Login.fxml"); }
    public static void mostrarMenuPrincipal()  { cambiarEscena("/view/MenuPrincipal.fxml"); }
    public static void mostrarGestionAlumnos() { cambiarEscena("/view/GestionAlumnos.fxml"); }
    public static void mostrarControlAcceso()  { cambiarEscena("/view/ControlAcceso.fxml"); }
    public static void mostrarHistorial()      { cambiarEscena("/view/HistorialAsistencia.fxml"); }

    public static void main(String[] args) { launch(args); }
}