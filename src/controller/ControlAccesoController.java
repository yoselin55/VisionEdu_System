package controller;

import dao.AlumnoDAO;
import dao.AsistenciaDAO;
import data.FaceRecognition;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import models.Alumno;
import models.Asistencia;
import main.Main;
import utils.IdiomaManager;
import utils.ThemeManager;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import java.awt.image.BufferedImage;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

public class ControlAccesoController {

    @FXML private ImageView imgCamera;
    @FXML private Button    btnIniciarEscaneo, btnDetenerEscaneo;
    @FXML private Label     lblEstado, lblAlumnoDetectado, lblHoraActual;
    @FXML private Label     lblTituloAcceso, lblCamTitle, lblEstadoSistema;
    @FXML private Label     lblHoraLabel, lblEstadoLabel, lblAlumnoLabel;
    @FXML private Button    btnVolverAcceso;

    private FaceRecognition faceRecognition;
    private AlumnoDAO       alumnoDAO;
    private AsistenciaDAO   asistenciaDAO;
    private VideoCapture    camera;
    private Timeline        timeline;
    private LocalDate       fechaUltimoChequeo;

    private int lastRecognizedId  = -1;
    private int framesDenegados   = 0;
    private int framesReconocidos = 0;
    private boolean enCooldown    = false;

    private static final int FRAMES_NECESARIOS = 3;

    // ── Horarios corregidos ──────────────────────────────────────────────────
    // Presente : antes de las 8:00 AM
    // Tarde    : entre 8:00 AM y 1:00 PM (13:00)
    // Falta    : después de la 1:00 PM (13:00)
    private static final LocalTime HORA_LIMITE_PUNTUAL = LocalTime.of(8,  0);  // 8:00 AM
    private static final LocalTime HORA_LIMITE_FALTA   = LocalTime.of(13, 0);  // 1:00 PM

    private final OpenCVFrameConverter.ToMat converterToMat   =
            new OpenCVFrameConverter.ToMat();
    private final Java2DFrameConverter        converterToImage =
            new Java2DFrameConverter();

    public void initialize() {
        faceRecognition = new FaceRecognition();
        faceRecognition.setModoOscuro(ThemeManager.isDarkMode());
        alumnoDAO      = new AlumnoDAO();
        asistenciaDAO  = new AsistenciaDAO();
        btnDetenerEscaneo.setDisable(true);
        fechaUltimoChequeo = LocalDate.now();
        asistenciaDAO.cerrarDiaAnterior();
        aplicarIdioma();
        actualizarHora();
    }

    private void aplicarIdioma() {
        if (lblTituloAcceso  != null) lblTituloAcceso.setText(
                IdiomaManager.t("menu.acceso").toUpperCase());
        if (btnVolverAcceso  != null) btnVolverAcceso.setText(
                IdiomaManager.t("btn.volver"));
        if (lblCamTitle      != null) lblCamTitle.setText(
                IdiomaManager.t("acceso.camara"));
        if (lblEstadoSistema != null) lblEstadoSistema.setText(
                IdiomaManager.t("acceso.estado_sistema"));
        if (lblHoraLabel     != null) lblHoraLabel.setText(
                IdiomaManager.t("acceso.hora_actual"));
        if (lblEstadoLabel   != null) lblEstadoLabel.setText(
                IdiomaManager.t("acceso.estado"));
        if (lblAlumnoLabel   != null) lblAlumnoLabel.setText(
                IdiomaManager.t("acceso.alumno_detectado"));
        if (btnIniciarEscaneo != null) btnIniciarEscaneo.setText(
                IdiomaManager.t("acceso.iniciar"));
        if (btnDetenerEscaneo != null) btnDetenerEscaneo.setText(
                IdiomaManager.t("acceso.detener"));
        if (lblEstado != null) lblEstado.setText(
                IdiomaManager.t("acceso.detenido"));
    }

    private void actualizarHora() {
        Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalTime ahora = LocalTime.now();
            LocalDate hoy   = LocalDate.now();

            lblHoraActual.setText(ahora.toString().substring(0, 8));

            if (!hoy.equals(fechaUltimoChequeo)) {
                fechaUltimoChequeo = hoy;
                asistenciaDAO.cerrarDiaAnterior();
                System.out.println("Nuevo dia: " + hoy);
            }

            // Rojo si ya pasó la hora límite de falta (1 PM)
            lblHoraActual.setStyle(ahora.isAfter(HORA_LIMITE_FALTA)
                    ? "-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#ef5350;"
                    : "-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#e8eaf6;");
        }));
        reloj.setCycleCount(Animation.INDEFINITE);
        reloj.play();
    }

    @FXML
    public void handleIniciarEscaneo() {
        camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            lblEstado.setText("Error: No se pudo abrir la camara");
            return;
        }
        btnIniciarEscaneo.setDisable(true);
        btnDetenerEscaneo.setDisable(false);
        lblEstado.setText(IdiomaManager.t("acceso.escaneando"));
        lblEstado.setStyle(
                "-fx-font-size:17px;-fx-text-fill:#66bb6a;-fx-font-weight:bold;");
        framesDenegados   = 0;
        framesReconocidos = 0;
        lastRecognizedId  = -1;
        enCooldown        = false;

        timeline = new Timeline(
                new KeyFrame(Duration.millis(100), e -> procesarFrame()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void procesarFrame() {
        Mat frame = new Mat();
        if (!camera.read(frame) || frame.empty()) return;

        Mat frameVista = new Mat();
        resize(frame, frameVista, new Size(640, 480));
        frameVista = faceRecognition.detectFace(frameVista);
        imgCamera.setImage(matToImage(frameVista));

        if (enCooldown) return;

        int id = faceRecognition.recognizeFace(frame);

        if (id > 0) {
            framesDenegados = 0;
            framesReconocidos++;
            if (framesReconocidos >= FRAMES_NECESARIOS && id != lastRecognizedId) {
                framesReconocidos = 0;
                lastRecognizedId  = id;
                enCooldown        = true;
                final int idFinal = id;
                new Thread(() -> procesarAsistencia(idFinal)).start();
                Timeline cooldown = new Timeline(
                        new KeyFrame(Duration.seconds(4), e -> {
                            lastRecognizedId  = -1;
                            framesReconocidos = 0;
                            enCooldown        = false;
                        }));
                cooldown.play();
            }

        } else if (id == -2) {
            framesReconocidos = 0;
            framesDenegados++;
            if (framesDenegados >= FRAMES_NECESARIOS && lastRecognizedId != -2) {
                framesDenegados  = 0;
                lastRecognizedId = -2;
                enCooldown       = true;
                Platform.runLater(this::mostrarAccesoDenegado);
                Timeline cooldown = new Timeline(
                        new KeyFrame(Duration.seconds(4), e -> {
                            lastRecognizedId = -1;
                            framesDenegados  = 0;
                            enCooldown       = false;
                            Platform.runLater(() -> {
                                lblAlumnoDetectado.setText("---");
                                lblAlumnoDetectado.setStyle(
                                        "-fx-font-size:16px;-fx-text-fill:#e8eaf6;");
                                lblEstado.setText(
                                        IdiomaManager.t("acceso.escaneando"));
                                lblEstado.setStyle(
                                        "-fx-font-size:17px;-fx-text-fill:#66bb6a;" +
                                                "-fx-font-weight:bold;");
                            });
                        }));
                cooldown.play();
            }

        } else {
            framesDenegados   = 0;
            framesReconocidos = 0;
        }
    }

    private void mostrarAccesoDenegado() {
        lblAlumnoDetectado.setText(IdiomaManager.t("acceso.denegado"));
        lblAlumnoDetectado.setStyle(
                "-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#ef5350;");
        lblEstado.setText(IdiomaManager.t("acceso.no_registrado"));
        lblEstado.setStyle(
                "-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#ef5350;");
        reproducirSonidoError();
        decirConVoz(IdiomaManager.t("voz.denegado"));
    }

    private void procesarAsistencia(int idAlumno) {
        LocalTime horaActual = LocalTime.now();
        String estado;
        if (horaActual.isBefore(HORA_LIMITE_PUNTUAL)) {
            estado = "Presente";
        } else if (horaActual.isBefore(HORA_LIMITE_FALTA)) {
            estado = "Tarde";
        } else {
            estado = "Falta";
        }

        if (asistenciaDAO.verificarAsistenciaHoy(idAlumno)) {
            Platform.runLater(() -> {
                lblAlumnoDetectado.setText(IdiomaManager.t("acceso.ya_registro"));
                lblAlumnoDetectado.setStyle(
                        "-fx-font-size:20px;-fx-text-fill:#ffa726;-fx-font-weight:bold;");
                lblEstado.setText(IdiomaManager.t("acceso.ya_registro"));
                lblEstado.setStyle(
                        "-fx-font-size:15px;-fx-text-fill:#ffa726;-fx-font-weight:bold;");
            });
            reproducirSonidoError();
            decirConVoz(IdiomaManager.t("voz.ya_registro"));
            return;
        }

        Alumno alumno = alumnoDAO.buscarAlumnoPorId(idAlumno);
        if (alumno == null) return;

        String estadoFinal = estado;
        Platform.runLater(() -> {
            lblAlumnoDetectado.setText(
                    IdiomaManager.t("acceso.bienvenido") + alumno.getNombreCompleto());
            String color = switch (estadoFinal) {
                case "Presente" -> "#66bb6a";
                case "Tarde"    -> "#ffa726";
                default         -> "#ef5350";
            };
            lblAlumnoDetectado.setStyle(
                    "-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
            lblEstado.setText(
                    IdiomaManager.t("estado." + estadoFinal.toLowerCase()));
            lblEstado.setStyle(
                    "-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        });

        Asistencia asis = new Asistencia(idAlumno,
                new Date(System.currentTimeMillis()),
                Time.valueOf(horaActual), estado);

        if (asistenciaDAO.registrarAsistencia(asis)) {
            if ("Falta".equals(estado)) {
                reproducirSonidoError();
                decirConVoz("Registro tardio. Falta. " + alumno.getNombre());
            } else {
                reproducirSonidoExito();
                decirConVoz(IdiomaManager.t("voz.bienvenido") + alumno.getNombre()
                        + IdiomaManager.t("voz.estado") + estadoFinal);
            }
        }
    }

    private void reproducirSonidoExito() {
        new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    Thread.sleep(150);
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void reproducirSonidoError() {
        new Thread(() -> {
            try {
                java.awt.Toolkit.getDefaultToolkit().beep();
                Thread.sleep(300);
                java.awt.Toolkit.getDefaultToolkit().beep();
            } catch (Exception ignored) {}
        }).start();
    }

    private void decirConVoz(String mensaje) {
        new Thread(() -> {
            try {
                String cmd = "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                        "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$s.Speak('" + mensaje.replace("'", "") + "')\"";
                Runtime.getRuntime().exec(cmd).waitFor();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    public void handleDetenerEscaneo() {
        if (timeline != null) timeline.stop();
        if (camera   != null) camera.release();
        btnIniciarEscaneo.setDisable(false);
        btnDetenerEscaneo.setDisable(true);
        imgCamera.setImage(null);
        framesDenegados   = 0;
        framesReconocidos = 0;
        lastRecognizedId  = -1;
        enCooldown        = false;
        lblEstado.setText(IdiomaManager.t("acceso.detenido"));
        lblEstado.setStyle(
                "-fx-font-size:20px;-fx-text-fill:#ef5350;-fx-font-weight:bold;");
        lblAlumnoDetectado.setText("---");
    }

    @FXML
    public void handleVolver() {
        handleDetenerEscaneo();
        Main.mostrarMenuPrincipal();
    }

    private Image matToImage(Mat mat) {
        try {
            BufferedImage bi = converterToImage.convert(converterToMat.convert(mat));
            return bi != null ? SwingFXUtils.toFXImage(bi, null) : null;
        } catch (Exception e) { return null; }
    }
}