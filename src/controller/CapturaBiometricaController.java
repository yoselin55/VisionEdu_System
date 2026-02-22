package controller;

import dao.AlumnoDAO;
import data.FaceRecognition;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Alumno;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import utils.ThemeManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class CapturaBiometricaController {

    @FXML private Label     lblTitulo, lblInstruccion, lblContador;
    @FXML private ImageView imgCamera;
    @FXML private ProgressBar progressBar;
    @FXML private Button    btnCapturar, btnCerrar;

    private FaceRecognition faceRecognition;
    private AlumnoDAO       alumnoDAO;
    private Alumno          alumno;

    private VideoCapture camera;
    private Timeline     timelineUI;

    private final AtomicBoolean camaraActiva = new AtomicBoolean(false);
    private final AtomicBoolean capturando   = new AtomicBoolean(false);
    private final AtomicBoolean hayRostro    = new AtomicBoolean(false); // ← conservado

    private GestionAlumnosController gestionAlumnosController;

    private final OpenCVFrameConverter.ToMat converterToMat   =
            new OpenCVFrameConverter.ToMat();
    private final Java2DFrameConverter        converterToImage =
            new Java2DFrameConverter();

    private volatile Mat frameActual = null;

    public CapturaBiometricaController() {
        faceRecognition = new FaceRecognition();
        alumnoDAO       = new AlumnoDAO();
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
        if (lblTitulo != null)
            lblTitulo.setText("Captura Biometrica - " + alumno.getNombreCompleto());
    }

    public void setGestionAlumnosController(GestionAlumnosController c) {
        this.gestionAlumnosController = c;
    }

    @FXML
    private void initialize() {
        faceRecognition.setModoOscuro(ThemeManager.isDarkMode());
        progressBar.setProgress(0);
        lblContador.setText("Foto: 0/1");
        btnCapturar.setDisable(true);
        agregarEfectoHover(btnCapturar);
        agregarEfectoHover(btnCerrar);
        lblInstruccion.setText("Iniciando camara...");

        Platform.runLater(() -> {
            if (btnCerrar.getScene() != null)
                ThemeManager.init(btnCerrar.getScene());
        });

        Thread hiloApertura = new Thread(() -> {
            VideoCapture cam = new VideoCapture();

            long inicio = System.currentTimeMillis();
            cam.open(0);

            // Esperar máximo 8 segundos
            while (!cam.isOpened()
                    && System.currentTimeMillis() - inicio < 8000) {
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }

            if (!cam.isOpened()) {
                Platform.runLater(() ->
                        lblInstruccion.setText(
                                "No se pudo acceder a la camara. Verifique que esté conectada."));
                return;
            }

            cam.set(3, 640);
            cam.set(4, 480);

            Mat descartar = new Mat();
            for (int i = 0; i < 5; i++) {
                cam.read(descartar);
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }

            camera = cam;
            camaraActiva.set(true);

            Platform.runLater(() -> {
                btnCapturar.setDisable(false);
                lblInstruccion.setText(
                        "Coloque su rostro frente a la camara y presione Capturar");
                iniciarHiloLectura();
                iniciarActualizacionUI();
            });
        });
        hiloApertura.setDaemon(true);
        hiloApertura.start();
    }

    private void iniciarHiloLectura() {
        Thread hiloLectura = new Thread(() -> {
            while (camaraActiva.get()) {
                if (!capturando.get() && camera != null && camera.isOpened()) {
                    Mat frame = new Mat();
                    if (camera.read(frame) && !frame.empty()) {
                        Mat conDeteccion = faceRecognition.detectFace(frame.clone());
                        // Actualizar flag de rostro detectado ← conservado
                        hayRostro.set(tieneRostroEnFrame(frame));
                        frameActual = conDeteccion;
                    }
                }
                try { Thread.sleep(33); } catch (InterruptedException ignored) {}
            }
        });
        hiloLectura.setDaemon(true);
        hiloLectura.start();
    }

    private void iniciarActualizacionUI() {
        timelineUI = new Timeline(
                new KeyFrame(Duration.millis(50), e -> {
                    Mat f = frameActual;
                    if (f != null && !f.empty()) {
                        Image img = matToImage(f);
                        if (img != null) imgCamera.setImage(img);
                    }
                }));
        timelineUI.setCycleCount(Animation.INDEFINITE);
        timelineUI.play();
    }

    private boolean tieneRostroEnFrame(Mat frame) {
        try {
            int id = faceRecognition.recognizeFace(frame);
            return id != -1 || id == -2;
        } catch (Exception e) {
            return false;
        }
    }


    @FXML
    private void handleCapturar() {
        if (!camaraActiva.get() || alumno == null || capturando.get()) return;
        capturando.set(true);
        btnCapturar.setDisable(true);
        lblInstruccion.setText("Verificando rostro...");
        progressBar.setProgress(0.1);

        new Thread(() -> {
            try {
                Mat frameSnap = tomarFrameFresco();

                if (frameSnap == null || frameSnap.empty()) {
                    Platform.runLater(() -> {
                        lblInstruccion.setText(
                                "No se detectó rostro. Colóquese frente a la cámara.");
                        lblInstruccion.setStyle("-fx-text-fill: #ffa726;");
                        resetearEstado();
                    });
                    return;
                }

                Platform.runLater(() -> {
                    lblInstruccion.setText("Verificando similitud biométrica...");
                    progressBar.setProgress(0.25);
                });

                int idReconocido  = faceRecognition.recognizeFace(frameSnap);
                boolean duplicado = idReconocido > 0
                        && idReconocido != alumno.getIdAlumno();

                if (duplicado) {
                    Platform.runLater(() ->
                            mostrarDialogoDuplicado(frameSnap, idReconocido));
                } else {
                    procederCaptura(frameSnap);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    lblInstruccion.setText("Error en verificación. Intente nuevamente.");
                    resetearEstado();
                });
            }
        }).start();
    }

    private Mat tomarFrameFresco() {
        for (int i = 0; i < 20; i++) {
            if (camera != null && camera.isOpened()) {
                Mat f = new Mat();
                if (camera.read(f) && !f.empty()) return f;
            }
            try { Thread.sleep(40); } catch (InterruptedException ignored) {}
        }
        return null;
    }

    private void mostrarDialogoDuplicado(Mat frameSnap, int idSimilar) {
        Alumno similar = alumnoDAO.buscarAlumnoPorId(idSimilar);

        if (similar == null || !new File(
                similar.getFotoPath() != null
                        ? similar.getFotoPath() : "").exists()) {
            new Thread(() -> procederCaptura(frameSnap)).start();
            return;
        }

        String nombre = similar.getNombreCompleto();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Similitud Detectada");
        confirm.setHeaderText("Rostro similar a: " + nombre);
        confirm.setContentText("¿Son mellizos o gemelos? ¿Desea continuar?");
        ButtonType btnSi = new ButtonType("Sí, continuar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("Cancelar",       ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnSi, btnNo);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == btnSi) {
            new Thread(() -> procederCaptura(frameSnap)).start();
        } else {
            lblInstruccion.setText("Captura cancelada.");
            lblInstruccion.setStyle("-fx-text-fill: #ffa726;");
            resetearEstado();
        }
    }

    private void procederCaptura(Mat frameSnap) {
        try {
            for (int i = 3; i >= 1; i--) {
                final int seg = i;
                Platform.runLater(() -> {
                    lblInstruccion.setText("Capturando en " + seg + "...");
                    lblInstruccion.setStyle("-fx-text-fill: #e8eaf6;");
                    progressBar.setProgress((4.0 - seg) / 4.0);
                });
                Thread.sleep(700);
            }

            Platform.runLater(() -> {
                lblInstruccion.setText("Guardando foto...");
                progressBar.setProgress(0.75);
            });

            String nombreArchivo = alumno.getNombre().replace(" ", "")
                    + "_" + alumno.getApellido().replace(" ", "")
                    + "_" + alumno.getIdAlumno();

            boolean guardado = guardarFotoDesdeFrame(frameSnap, nombreArchivo);

            if (!guardado) {
                Mat frameFresco = tomarFrameFresco();
                if (frameFresco != null)
                    guardado = guardarFotoDesdeFrame(frameFresco, nombreArchivo);
            }

            if (!guardado) {
                Platform.runLater(() -> {
                    lblInstruccion.setText(
                            "No se pudo guardar el rostro. Intente nuevamente.");
                    lblInstruccion.setStyle("-fx-text-fill: #ef5350;");
                    resetearEstado();
                });
                return;
            }

            Platform.runLater(() -> {
                lblInstruccion.setText("Entrenando modelo biométrico...");
                progressBar.setProgress(0.88);
            });

            faceRecognition.trainModel();

            String fotoPath = "src/data/images/" + nombreArchivo + ".jpg";
            alumno.setFotoPath(fotoPath);
            alumnoDAO.actualizarAlumno(alumno);

            Platform.runLater(() -> {
                progressBar.setProgress(1.0);
                lblContador.setText("Foto: 1/1");
                lblInstruccion.setText("✔  Rostro registrado correctamente.");
                lblInstruccion.setStyle(
                        "-fx-text-fill: #66bb6a; -fx-font-weight: bold;");
                btnCapturar.setDisable(true);
                btnCapturar.setText("✔  Completado");
                btnCapturar.setStyle(
                        "-fx-background-color: #2e7d32; -fx-text-fill: white;" +
                                "-fx-font-weight: bold; -fx-background-radius: 10;");
                if (gestionAlumnosController != null)
                    gestionAlumnosController.refrescarDespuesDeCaptura();
            });

        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                lblInstruccion.setText("Error al procesar. Intente nuevamente.");
                lblInstruccion.setStyle("-fx-text-fill: #ef5350;");
                resetearEstado();
            });
        }
    }

    private boolean guardarFotoDesdeFrame(Mat frame, String nombreArchivo) {
        try {
            Mat gray    = new Mat();
            Mat resized = new Mat();
            cvtColor(frame, gray, COLOR_BGR2GRAY);
            resize(gray, resized, new Size(200, 200));
            String path = "src/data/images/" + nombreArchivo + ".jpg";
            boolean ok  = imwrite(path, resized);
            if (ok) System.out.println("Foto guardada: " + path);
            else    System.err.println("Error al guardar: " + path);
            return ok;
        } catch (Exception e) {
            System.err.println("Excepcion guardando foto: " + e.getMessage());
            return false;
        }
    }

    private void resetearEstado() {
        capturando.set(false);
        btnCapturar.setDisable(false);
        progressBar.setProgress(0);
        lblInstruccion.setStyle("");
    }

    private void agregarEfectoHover(Button boton) {
        boton.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), boton);
            st.setToX(1.05); st.setToY(1.05); st.play();
        });
        boton.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), boton);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
    }

    @FXML
    private void handleCerrar() {
        detenerCamara();
        ((Stage) btnCerrar.getScene().getWindow()).close();
    }

    private void detenerCamara() {
        camaraActiva.set(false);
        capturando.set(false);
        if (timelineUI != null) timelineUI.stop();
        try { Thread.sleep(150); } catch (InterruptedException ignored) {}
        if (camera != null) { camera.release(); camera = null; }
        frameActual = null;
    }

    private Image matToImage(Mat mat) {
        try {
            BufferedImage bi = converterToImage.convert(converterToMat.convert(mat));
            return bi != null ? SwingFXUtils.toFXImage(bi, null) : null;
        } catch (Exception e) { return null; }
    }
}