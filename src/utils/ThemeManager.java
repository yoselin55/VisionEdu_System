package utils;

import javafx.scene.Scene;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String CSS_PATH = "/styles/style.css";
    private static final Preferences prefs = Preferences.userRoot().node("visionedu/theme");
    private static boolean isDarkMode;

    public static void init(Scene scene) {
        if (scene == null) return;
        isDarkMode = prefs.getBoolean("darkMode", true);
        scene.getStylesheets().clear();
        try {
            String cssUrl = ThemeManager.class.getResource(CSS_PATH).toExternalForm();
            scene.getStylesheets().add(cssUrl);
        } catch (Exception e) {
            System.err.println("Error cargando CSS: " + e.getMessage());
        }
        applyTheme(scene);
    }

    public static void toggleTheme(Scene scene) {
        if (scene == null) return;
        isDarkMode = !isDarkMode;
        prefs.putBoolean("darkMode", isDarkMode);
        applyTheme(scene);
    }

    private static void applyTheme(Scene scene) {
        if (scene == null || scene.getRoot() == null) return;
        var styleClasses = scene.getRoot().getStyleClass();
        if (isDarkMode) {
            styleClasses.remove("light-mode");
        } else {
            if (!styleClasses.contains("light-mode")) {
                styleClasses.add("light-mode");
            }
        }
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }
}