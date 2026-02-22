package controller;

import dao.AlumnoDAO;
import data.FaceRecognition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Alumno;
import main.Main;
import utils.IdiomaManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class GestionAlumnosController {

    @FXML private TextField txtNombre, txtApellido, txtDni, txtBuscar;
    @FXML private ComboBox<String> cmbGrado;
    @FXML private TableView<Alumno> tablaAlumnos;
    @FXML private TableColumn<Alumno, Integer> colId;
    @FXML private TableColumn<Alumno, String>  colNombre, colApellido, colDni, colGrado, colFechaRegistro;
    @FXML private Label     lblTitulo;
    @FXML private Label     lblDatosAlumno;
    @FXML private Button    btnVolver;
    @FXML private Button    btnRegistrar;
    @FXML private Button    btnCapturar;
    @FXML private Button    btnActualizar;
    @FXML private Button    btnEliminar;
    @FXML private Button    btnBuscar;

    private AlumnoDAO alumnoDAO;
    private FaceRecognition faceRecognition;
    private ObservableList<Alumno> listaAlumnos;
    private Alumno alumnoSeleccionado;
    private static final String TRAINING_DATA_PATH = "src/data/images/";
    public GestionAlumnosController() {
        alumnoDAO      = new AlumnoDAO();
        faceRecognition = new FaceRecognition();
        listaAlumnos   = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        aplicarIdioma();

        cmbGrado.getItems().addAll(
                "1° Primaria","2° Primaria","3° Primaria","4° Primaria",
                "5° Primaria","6° Primaria",
                "1° Secundaria","2° Secundaria","3° Secundaria",
                "4° Secundaria","5° Secundaria"
        );

        colId.setCellValueFactory(new PropertyValueFactory<>("idAlumno"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colGrado.setCellValueFactory(new PropertyValueFactory<>("grado"));
        colFechaRegistro.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));

        agregarCapitalizacionAutomatica(txtNombre);
        agregarCapitalizacionAutomatica(txtApellido);
        sincronizarArchivosConBD();
        cargarAlumnos();

        tablaAlumnos.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> { if (newVal != null) seleccionarAlumno(newVal); });
    }

    private void aplicarIdioma() {
        // Título
        if (lblTitulo    != null) lblTitulo.setText(IdiomaManager.t("gestion.titulo"));
        if (lblDatosAlumno != null) lblDatosAlumno.setText(IdiomaManager.t("gestion.datos"));

        // Botones
        if (btnVolver    != null) btnVolver.setText(IdiomaManager.t("btn.volver"));
        if (btnRegistrar != null) btnRegistrar.setText(IdiomaManager.t("gestion.registrar"));
        if (btnCapturar  != null) btnCapturar.setText(IdiomaManager.t("gestion.capturar"));
        if (btnActualizar!= null) btnActualizar.setText(IdiomaManager.t("gestion.actualizar"));
        if (btnEliminar  != null) btnEliminar.setText(IdiomaManager.t("gestion.eliminar"));
        if (btnBuscar    != null) btnBuscar.setText(IdiomaManager.t("gestion.buscar"));

        // Columnas de la tabla
        if (colId            != null) colId.setText("ID");
        if (colNombre        != null) colNombre.setText(IdiomaManager.t("alumno.nombre"));
        if (colApellido      != null) colApellido.setText(IdiomaManager.t("alumno.apellido"));
        if (colDni           != null) colDni.setText(IdiomaManager.t("alumno.dni"));
        if (colGrado         != null) colGrado.setText(IdiomaManager.t("alumno.grado"));
        if (colFechaRegistro != null) colFechaRegistro.setText(IdiomaManager.t("gestion.fecha_registro"));

        // Placeholders de campos
        if (txtNombre  != null) txtNombre.setPromptText(IdiomaManager.t("alumno.nombre"));
        if (txtApellido!= null) txtApellido.setPromptText(IdiomaManager.t("alumno.apellido"));
        if (txtDni     != null) txtDni.setPromptText(IdiomaManager.t("alumno.dni") + " (8 " + IdiomaManager.t("gestion.digitos") + ")");
        if (txtBuscar  != null) txtBuscar.setPromptText(IdiomaManager.t("gestion.buscar_placeholder"));
        if (cmbGrado   != null) cmbGrado.setPromptText(IdiomaManager.t("gestion.seleccionar_grado"));
    }

    private void sincronizarArchivosConBD() {
        new Thread(() -> {
            try {
                List<Alumno> todos = alumnoDAO.listarAlumnos();
                int limpiados = 0;
                for (Alumno alumno : todos) {
                    if (alumno.getFotoPath() != null && !alumno.getFotoPath().isEmpty()) {
                        if (!new File(alumno.getFotoPath()).exists()) {
                            alumno.setFotoPath("");
                            alumnoDAO.actualizarAlumno(alumno);
                            limpiados++;
                        }
                    }
                }
                if (limpiados > 0) {
                    faceRecognition.trainModel();
                    Platform.runLater(this::cargarAlumnos);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void agregarCapitalizacionAutomatica(TextField tf) {
        tf.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                String cap = capitalizarPalabras(newVal);
                if (!newVal.equals(cap)) {
                    int pos = tf.getCaretPosition();
                    tf.setText(cap);
                    tf.positionCaret(Math.min(pos, cap.length()));
                }
            }
        });
    }

    private String capitalizarPalabras(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        String[] palabras = texto.split(" ", -1);
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < palabras.length; i++) {
            if (!palabras[i].isEmpty())
                res.append(Character.toUpperCase(palabras[i].charAt(0)))
                        .append(palabras[i].substring(1).toLowerCase());
            if (i < palabras.length - 1) res.append(" ");
        }
        return res.toString();
    }

    private void cargarAlumnos() {
        listaAlumnos.setAll(alumnoDAO.listarAlumnos());
        tablaAlumnos.setItems(listaAlumnos);
    }

    private void seleccionarAlumno(Alumno alumno) {
        this.alumnoSeleccionado = alumno;
        txtNombre.setText(alumno.getNombre());
        txtApellido.setText(alumno.getApellido());
        txtDni.setText(alumno.getDni());
        cmbGrado.setValue(alumno.getGrado());
    }

    @FXML private void handleVolver() { Main.mostrarMenuPrincipal(); }

    @FXML
    private void handleRegistrar() {
        if (!validarCampos()) return;
        if (alumnoDAO.existeDNI(txtDni.getText().trim())) {
            mostrarAlerta(IdiomaManager.t("alerta.error"),
                    IdiomaManager.t("gestion.dni_existente"), Alert.AlertType.ERROR);
            return;
        }
        Alumno a = new Alumno();
        a.setNombre(txtNombre.getText().trim());
        a.setApellido(txtApellido.getText().trim());
        a.setDni(txtDni.getText().trim());
        a.setGrado(cmbGrado.getValue());
        a.setFotoPath("");

        if (alumnoDAO.registrarAlumno(a)) {
            cargarAlumnos();
            Optional<ButtonType> resp = mostrarConfirmacion(
                    IdiomaManager.t("gestion.capturar"),
                    IdiomaManager.t("gestion.capturar_ahora"));
            if (resp.isPresent() && resp.get() == ButtonType.OK) {
                alumnoSeleccionado = alumnoDAO.buscarAlumnoPorNombre(a.getDni()).get(0);
                handleCapturarBiometrica();
            }
            limpiarCampos();
        }
    }

    @FXML
    private void handleCapturarBiometrica() {
        if (alumnoSeleccionado == null) {
            mostrarAlerta(IdiomaManager.t("alerta.error"),
                    IdiomaManager.t("gestion.seleccionar_primero"), Alert.AlertType.ERROR);
            return;
        }
        if (alumnoSeleccionado.getFotoPath() != null
                && !alumnoSeleccionado.getFotoPath().isEmpty()
                && new File(alumnoSeleccionado.getFotoPath()).exists()) {
            mostrarAlerta(IdiomaManager.t("gestion.registro_bloqueado"),
                    IdiomaManager.t("gestion.ya_tiene_biometrico"), Alert.AlertType.WARNING);
            return;
        }
        abrirVentanaCaptura();
    }

    private void abrirVentanaCaptura() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/CapturaBiometrica.fxml"));
            Parent root = loader.load();
            CapturaBiometricaController ctrl = loader.getController();
            ctrl.setAlumno(alumnoSeleccionado);
            ctrl.setGestionAlumnosController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setWidth(1100);
            stage.setHeight(720);
            stage.setMaximized(true);
            stage.setTitle(IdiomaManager.t("captura.titulo") + " - "
                    + alumnoSeleccionado.getNombreCompleto());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void refrescarDespuesDeCaptura() {
        cargarAlumnos();
        mostrarAlerta(IdiomaManager.t("alerta.exito"),
                IdiomaManager.t("captura.exito"), Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleActualizar() {
        if (alumnoSeleccionado == null || !validarCampos()) return;
        alumnoSeleccionado.setNombre(txtNombre.getText().trim());
        alumnoSeleccionado.setApellido(txtApellido.getText().trim());
        alumnoSeleccionado.setDni(txtDni.getText().trim());
        alumnoSeleccionado.setGrado(cmbGrado.getValue());
        if (alumnoDAO.actualizarAlumno(alumnoSeleccionado)) {
            cargarAlumnos();
            limpiarCampos();
        }
    }

    @FXML
    private void handleEliminar() {
        if (alumnoSeleccionado == null) return;
        Optional<ButtonType> conf = mostrarConfirmacion(
                IdiomaManager.t("gestion.eliminar"),
                IdiomaManager.t("gestion.confirmar_eliminar") + " "
                        + alumnoSeleccionado.getNombreCompleto() + "?");
        if (conf.isEmpty() || conf.get() != ButtonType.OK) return;

        String fotoPath = alumnoSeleccionado.getFotoPath();
        if (fotoPath != null && !fotoPath.isEmpty()) {
            File foto = new File(fotoPath);
            if (foto.exists()) foto.delete();
        }
        String rutaConvencional = TRAINING_DATA_PATH
                + alumnoSeleccionado.getNombre().replace(" ","") + "_"
                + alumnoSeleccionado.getApellido().replace(" ","") + "_"
                + alumnoSeleccionado.getIdAlumno() + ".jpg";
        new File(rutaConvencional).delete();

        if (alumnoDAO.eliminarAlumno(alumnoSeleccionado.getIdAlumno())) {
            faceRecognition.trainModel();
            cargarAlumnos();
            limpiarCampos();
            mostrarAlerta(IdiomaManager.t("alerta.exito"),
                    IdiomaManager.t("gestion.eliminado_ok"), Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleBuscar() {
        String criterio = txtBuscar.getText().trim();
        if (criterio.isEmpty()) cargarAlumnos();
        else {
            listaAlumnos.setAll(alumnoDAO.buscarAlumnoPorNombre(criterio));
            tablaAlumnos.setItems(listaAlumnos);
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()
                || txtApellido.getText().trim().isEmpty()
                || txtDni.getText().trim().length() != 8
                || cmbGrado.getValue() == null) {
            mostrarAlerta(IdiomaManager.t("alerta.error"),
                    IdiomaManager.t("gestion.campos_invalidos"), Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        txtNombre.clear(); txtApellido.clear();
        txtDni.clear(); cmbGrado.setValue(null);
        txtBuscar.clear();
        alumnoSeleccionado = null;
        tablaAlumnos.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String t, String m, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m);
        a.showAndWait();
    }

    private Optional<ButtonType> mostrarConfirmacion(String t, String m) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m);
        return a.showAndWait();
    }
}