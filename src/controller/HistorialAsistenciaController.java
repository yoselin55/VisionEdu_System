package controller;

import dao.AlumnoDAO;
import dao.AsistenciaDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Alumno;
import models.Asistencia;
import main.Main;
import services.ReporteService;
import utils.IdiomaManager;
import utils.ThemeManager;
import java.io.File;
import java.sql.Date;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
public class HistorialAsistenciaController {
    @FXML private DatePicker  dpFecha;
    @FXML private TextField   txtIdAlumno;
    @FXML private TableView<Asistencia>        tablaAsistencias;
    @FXML private TableColumn<Asistencia, Integer> colId;
    @FXML private TableColumn<Asistencia, Integer> colFoto;
    @FXML private TableColumn<Asistencia, String>  colNombreAlumno;
    @FXML private TableColumn<Asistencia, String>  colDia;
    @FXML private TableColumn<Asistencia, Date>    colFecha;
    @FXML private TableColumn<Asistencia, Time>    colHora;
    @FXML private TableColumn<Asistencia, String>  colEstado;
    @FXML private Label lblTotalPresentes, lblTotalTarde, lblTotalRegistros, lblTotalFaltas;
    @FXML private Label  lblTituloHistorial;
    @FXML private Label  lblFechaLabel;
    @FXML private Label  lblBuscarLabel;
    @FXML private Label  lblStatTotal;
    @FXML private Label  lblStatPresentes;
    @FXML private Label  lblStatTardanzas;
    @FXML private Label  lblStatFaltas;
    @FXML private Label  lblPlaceholder;

    // Botones traducibles
    @FXML private Button btnVolverHistorial;
    @FXML private Button btnFiltrar;
    @FXML private Button btnTodos;
    @FXML private Button btnExportar;

    private AsistenciaDAO asistenciaDAO;
    private AlumnoDAO     alumnoDAO;
    private ObservableList<Asistencia> listaAsistencias;
    private ReporteService reporteService;

    public HistorialAsistenciaController() {
        asistenciaDAO    = new AsistenciaDAO();
        alumnoDAO        = new AlumnoDAO();
        listaAsistencias = FXCollections.observableArrayList();
        reporteService   = new ReporteService();
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> ThemeManager.init(tablaAsistencias.getScene()));
        aplicarIdioma();       // ← primero traducir
        configurarTabla();
        dpFecha.setValue(LocalDate.now());
        handleFiltrarPorFecha(null);
    }


    private void aplicarIdioma() {
        // Header
        if (lblTituloHistorial  != null) lblTituloHistorial.setText(
                IdiomaManager.t("historial.titulo"));
        if (btnVolverHistorial  != null) btnVolverHistorial.setText(
                IdiomaManager.t("btn.volver"));
        if (lblFechaLabel       != null) lblFechaLabel.setText(
                IdiomaManager.t("historial.fecha"));
        if (lblBuscarLabel      != null) lblBuscarLabel.setText(
                IdiomaManager.t("historial.buscar_alumno"));
        if (txtIdAlumno         != null) txtIdAlumno.setPromptText(
                IdiomaManager.t("historial.buscar_placeholder"));
        if (btnFiltrar          != null) btnFiltrar.setText(
                IdiomaManager.t("historial.filtrar"));
        if (btnTodos            != null) btnTodos.setText(
                IdiomaManager.t("historial.todos"));
        if (btnExportar         != null) btnExportar.setText(
                IdiomaManager.t("historial.exportar"));
        if (lblStatTotal        != null) lblStatTotal.setText(
                IdiomaManager.t("stat.total"));
        if (lblStatPresentes    != null) lblStatPresentes.setText(
                IdiomaManager.t("stat.presentes"));
        if (lblStatTardanzas    != null) lblStatTardanzas.setText(
                IdiomaManager.t("stat.tardanzas"));
        if (lblStatFaltas       != null) lblStatFaltas.setText(
                IdiomaManager.t("stat.faltas"));
        if (colId           != null) colId.setText("ID");
        if (colFoto         != null) colFoto.setText(
                IdiomaManager.t("historial.col_perfil"));
        if (colNombreAlumno != null) colNombreAlumno.setText(
                IdiomaManager.t("historial.col_alumno"));
        if (colDia          != null) colDia.setText(
                IdiomaManager.t("historial.col_dia"));
        if (colFecha        != null) colFecha.setText(
                IdiomaManager.t("historial.col_fecha"));
        if (colHora         != null) colHora.setText(
                IdiomaManager.t("historial.col_hora"));
        if (colEstado       != null) colEstado.setText(
                IdiomaManager.t("historial.col_estado"));

        // Placeholder tabla vacía
        if (lblPlaceholder  != null) lblPlaceholder.setText(
                IdiomaManager.t("historial.sin_registros"));
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idAsistencia"));

        // FOTO
        colFoto.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getIdAlumno()).asObject());
        colFoto.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            { iv.setFitWidth(36); iv.setFitHeight(36); iv.setPreserveRatio(true); }
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setGraphic(null); return; }
                Alumno al = alumnoDAO.buscarAlumnoPorId(id);
                if (al != null && al.getFotoPath() != null && !al.getFotoPath().isEmpty()) {
                    File f = new File(al.getFotoPath());
                    if (f.exists()) {
                        iv.setImage(new Image(f.toURI().toString()));
                        setGraphic(iv); setAlignment(Pos.CENTER); return;
                    }
                }
                Label ico = new Label("👤");
                ico.setStyle("-fx-font-size: 18px;");
                setGraphic(ico); setAlignment(Pos.CENTER);
            }
        });

        colNombreAlumno.setCellValueFactory(new PropertyValueFactory<>("nombreAlumno"));

        colDia.setCellValueFactory(cd -> {
            Date fecha = cd.getValue().getFecha();
            if (fecha == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(traducirDia(
                    fecha.toLocalDate().getDayOfWeek()));
        });
        colDia.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String dia, boolean empty) {
                super.updateItem(dia, empty);
                if (empty || dia == null) { setText(null); setStyle(""); return; }
                setText(dia);
                boolean finde = dia.equals(traducirDia(DayOfWeek.SATURDAY))
                        || dia.equals(traducirDia(DayOfWeek.SUNDAY));
                setStyle(finde
                        ? "-fx-text-fill: #78909c; -fx-font-style: italic;"
                        : "-fx-font-weight: bold;");
            }
        });

        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colHora.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Time hora, boolean empty) {
                super.updateItem(hora, empty);
                if (empty || hora == null) { setText("—"); return; }
                String h = hora.toString();
                setText(h.length() >= 5 ? h.substring(0,5) : h);
            }
        });

        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null); setGraphic(null); setStyle(""); return;
                }
                String textoEstado = IdiomaManager.t(
                        "estado." + estado.toLowerCase());
                Label badge = new Label(textoEstado.toUpperCase());
                badge.setStyle(switch (estado.toLowerCase()) {
                    case "presente" ->
                            "-fx-background-color:#1b5e20;-fx-text-fill:#a5d6a7;" +
                                    "-fx-font-weight:bold;-fx-font-size:11px;" +
                                    "-fx-padding:4 12 4 12;-fx-background-radius:20;";
                    case "tarde"    ->
                            "-fx-background-color:#e65100;-fx-text-fill:#ffe0b2;" +
                                    "-fx-font-weight:bold;-fx-font-size:11px;" +
                                    "-fx-padding:4 12 4 12;-fx-background-radius:20;";
                    case "falta"    ->
                            "-fx-background-color:#b71c1c;-fx-text-fill:#ffcdd2;" +
                                    "-fx-font-weight:bold;-fx-font-size:11px;" +
                                    "-fx-padding:4 12 4 12;-fx-background-radius:20;";
                    default         ->
                            "-fx-background-color:#37474f;-fx-text-fill:#b0bec5;" +
                                    "-fx-font-weight:bold;-fx-font-size:11px;" +
                                    "-fx-padding:4 12 4 12;-fx-background-radius:20;";
                });
                setGraphic(badge); setText(null); setAlignment(Pos.CENTER);
            }
        });

        tablaAsistencias.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Asistencia item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setStyle(""); return; }
                setStyle(switch (item.getEstado() != null
                        ? item.getEstado().toLowerCase() : "") {
                    case "falta"    -> "-fx-background-color: rgba(183,28,28,0.07);";
                    case "tarde"    -> "-fx-background-color: rgba(230,81,0,0.07);";
                    case "presente" -> "-fx-background-color: rgba(27,94,32,0.07);";
                    default         -> "";
                });
            }
        });

        tablaAsistencias.setItems(listaAsistencias);
    }
    private String traducirDia(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY    -> IdiomaManager.t("dia.lunes");
            case TUESDAY   -> IdiomaManager.t("dia.martes");
            case WEDNESDAY -> IdiomaManager.t("dia.miercoles");
            case THURSDAY  -> IdiomaManager.t("dia.jueves");
            case FRIDAY    -> IdiomaManager.t("dia.viernes");
            case SATURDAY  -> IdiomaManager.t("dia.sabado");
            case SUNDAY    -> IdiomaManager.t("dia.domingo");
        };
    }

    @FXML
    public void handleFiltrarPorFecha(ActionEvent event) {
        String busq = txtIdAlumno.getText().trim().toLowerCase();
        if (dpFecha.getValue() != null) {
            List<Asistencia> filtrados = asistenciaDAO
                    .filtrarPorFecha(Date.valueOf(dpFecha.getValue()));
            if (!busq.isEmpty()) {
                filtrados = filtrados.stream()
                        .filter(a -> (a.getNombreAlumno() != null &&
                                a.getNombreAlumno().toLowerCase().contains(busq))
                                || String.valueOf(a.getIdAlumno()).contains(busq))
                        .collect(Collectors.toList());
            }
            listaAsistencias.setAll(filtrados);
        } else if (!busq.isEmpty()) {
            try {
                listaAsistencias.setAll(
                        asistenciaDAO.filtrarPorAlumno(Integer.parseInt(busq)));
            } catch (NumberFormatException e) {
                listaAsistencias.setAll(asistenciaDAO.listarAsistencias().stream()
                        .filter(a -> a.getNombreAlumno() != null &&
                                a.getNombreAlumno().toLowerCase().contains(busq))
                        .collect(Collectors.toList()));
            }
        }
        actualizarEstadisticas();
    }

    @FXML
    public void handleMostrarTodos(ActionEvent event) {
        txtIdAlumno.clear();
        dpFecha.setValue(null);
        listaAsistencias.setAll(asistenciaDAO.listarAsistencias());
        actualizarEstadisticas();
    }

    @FXML
    public void handleExportarReporte(ActionEvent event) {
        if (listaAsistencias.isEmpty()) {
            mostrarAlerta(
                    IdiomaManager.t("stat.total"),
                    IdiomaManager.t("historial.sin_registros"),
                    Alert.AlertType.WARNING);
            return;
        }
        mostrarDialogoExportar();
    }

    private void mostrarDialogoExportar() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(IdiomaManager.t("dialogo.periodo"));
        dialog.setResizable(false);

        ToggleGroup tg = new ToggleGroup();
        RadioButton rbTodos = rb(IdiomaManager.t("periodo.todos"), tg);
        RadioButton rbAnio  = rb(IdiomaManager.t("periodo.anio"),  tg);
        RadioButton rbMes   = rb(IdiomaManager.t("periodo.mes"),   tg);
        RadioButton rbSem   = rb(IdiomaManager.t("periodo.semana"),tg);
        rbTodos.setSelected(true);

        Spinner<Integer> spAnio = new Spinner<>(2020, 2035, LocalDate.now().getYear());
        spAnio.setPrefWidth(100);
        HBox ctrlAnio = fila(IdiomaManager.t("periodo.anio") + ":", spAnio);
        ctrlAnio.setVisible(false); ctrlAnio.setManaged(false);

        Spinner<Integer> spMesAnio = new Spinner<>(2020, 2035, LocalDate.now().getYear());
        spMesAnio.setPrefWidth(90);
        ComboBox<String> cbMes = new ComboBox<>();
        cbMes.getItems().addAll("Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre");
        cbMes.setValue(LocalDate.now().getMonth()
                .getDisplayName(java.time.format.TextStyle.FULL, new Locale("es")));
        cbMes.setPrefWidth(140);
        HBox ctrlMes = fila(IdiomaManager.t("periodo.anio") + ":", spMesAnio,
                IdiomaManager.t("periodo.mes")  + ":", cbMes);
        ctrlMes.setVisible(false); ctrlMes.setManaged(false);

        DatePicker dpSem = new DatePicker(
                LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        dpSem.setPrefWidth(155);
        HBox ctrlSem = fila(IdiomaManager.t("periodo.semana") + ":", dpSem);
        ctrlSem.setVisible(false); ctrlSem.setManaged(false);

        tg.selectedToggleProperty().addListener((obs, o, n) -> {
            ctrlAnio.setVisible(n == rbAnio); ctrlAnio.setManaged(n == rbAnio);
            ctrlMes.setVisible(n == rbMes);   ctrlMes.setManaged(n == rbMes);
            ctrlSem.setVisible(n == rbSem);   ctrlSem.setManaged(n == rbSem);
            dialog.sizeToScene();
        });

        ToggleGroup fmtGroup = new ToggleGroup();
        RadioButton rbExcel = rb("Excel (.xlsx)", fmtGroup);
        RadioButton rbPdf   = rb("PDF (.pdf)",    fmtGroup);
        rbExcel.setSelected(true);
        HBox fmtBox = new HBox(20, rbExcel, rbPdf);
        fmtBox.setAlignment(Pos.CENTER_LEFT);

        Button btnExp = new Button(IdiomaManager.t("btn.exportar_guardar"));
        btnExp.setMaxWidth(Double.MAX_VALUE);
        btnExp.setStyle(
                "-fx-background-color: #1565c0; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 14px;" +
                        "-fx-background-radius: 10; -fx-cursor: hand; -fx-pref-height: 48;" +
                        "-fx-effect: dropshadow(gaussian,rgba(21,101,192,0.5),10,0,0,3);");

        Button btnCan = new Button(IdiomaManager.t("btn.cancelar"));
        btnCan.setMaxWidth(Double.MAX_VALUE);
        btnCan.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #ef5350;" +
                        "-fx-border-width: 1.5; -fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-text-fill: #ef5350; -fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-cursor: hand; -fx-pref-height: 40;");
        btnCan.setOnAction(e -> dialog.close());

        btnExp.setOnAction(e -> {
            try {
                List<Asistencia> listaExportar;
                String tituloPeriodo;
                List<Asistencia> todas = asistenciaDAO.listarAsistencias();

                if (rbAnio.isSelected()) {
                    int anio = spAnio.getValue();
                    listaExportar = reporteService.filtrarPorAnio(todas, anio);
                    tituloPeriodo = IdiomaManager.t("periodo.anio") + " " + anio;
                } else if (rbMes.isSelected()) {
                    int anio = spMesAnio.getValue();
                    int mes  = cbMes.getSelectionModel().getSelectedIndex() + 1;
                    listaExportar = reporteService.filtrarPorMes(todas, anio, mes);
                    tituloPeriodo = cbMes.getValue() + " " + anio;
                } else if (rbSem.isSelected()) {
                    LocalDate inicio = dpSem.getValue();
                    listaExportar   = reporteService.filtrarPorSemana(todas, inicio);
                    tituloPeriodo   = IdiomaManager.t("periodo.semana") + " "
                            + inicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + " - "
                            + inicio.plusDays(6).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    listaExportar = new java.util.ArrayList<>(listaAsistencias);
                    tituloPeriodo = IdiomaManager.t("periodo.todos");
                }

                if (listaExportar.isEmpty()) {
                    mostrarAlerta(IdiomaManager.t("stat.total"),
                            IdiomaManager.t("historial.sin_registros"),
                            Alert.AlertType.WARNING);
                    return;
                }

                boolean esExcel = rbExcel.isSelected();
                FileChooser fc = new FileChooser();
                fc.setTitle(IdiomaManager.t("dialogo.periodo"));
                fc.setInitialFileName("Asistencia_"
                        + tituloPeriodo.replace(" ","_").replace("/","-"));
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                        esExcel ? "Excel" : "PDF",
                        esExcel ? "*.xlsx" : "*.pdf"));
                File f = fc.showSaveDialog(dialog);

                if (f != null) {
                    if (esExcel)
                        reporteService.exportarExcelConPeriodo(
                                listaExportar, f.getAbsolutePath(), tituloPeriodo);
                    else
                        reporteService.exportarPDFConPeriodo(
                                listaExportar, f.getAbsolutePath(), tituloPeriodo);
                    dialog.close();
                    mostrarAlerta(IdiomaManager.t("alerta.exito"),
                            IdiomaManager.t("historial.exportado_ok") + listaExportar.size(),
                            Alert.AlertType.INFORMATION);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta(IdiomaManager.t("alerta.error"),
                        ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        VBox root = new VBox(12);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #1e2740;");

        root.getChildren().addAll(
                lbl(IdiomaManager.t("dialogo.periodo")),
                new Separator(),
                lbl(IdiomaManager.t("historial.periodo_label")),
                rbTodos, rbAnio, rbMes, rbSem,
                ctrlAnio, ctrlMes, ctrlSem,
                new Separator(),
                lbl(IdiomaManager.t("historial.formato_label")),
                fmtBox,
                new Separator(),
                btnExp, btnCan
        );

        dialog.setScene(new Scene(root, 430, 460));
        dialog.sizeToScene();
        dialog.showAndWait();
    }

    private RadioButton rb(String texto, ToggleGroup tg) {
        RadioButton rb = new RadioButton(texto);
        rb.setToggleGroup(tg);
        rb.setStyle("-fx-text-fill: #c5cae9; -fx-font-size: 13px;");
        return rb;
    }

    private Label lbl(String texto) {
        Label l = new Label(texto);
        l.setStyle(
                "-fx-text-fill: #9fa8da; -fx-font-size: 12px; -fx-font-weight: bold;");
        return l;
    }

    private HBox fila(Object... items) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        for (Object item : items) {
            if (item instanceof String) {
                Label l = new Label((String) item);
                l.setStyle("-fx-text-fill: #9fa8da; -fx-font-size: 12px;");
                box.getChildren().add(l);
            } else if (item instanceof javafx.scene.Node) {
                box.getChildren().add((javafx.scene.Node) item);
            }
        }
        return box;
    }

    @FXML
    public void handleVolver(ActionEvent event) { Main.mostrarMenuPrincipal(); }

    private void actualizarEstadisticas() {
        long total     = listaAsistencias.size();
        long presentes = listaAsistencias.stream()
                .filter(a -> "Presente".equalsIgnoreCase(a.getEstado())).count();
        long tardes    = listaAsistencias.stream()
                .filter(a -> "Tarde".equalsIgnoreCase(a.getEstado())).count();
        long faltas    = listaAsistencias.stream()
                .filter(a -> "Falta".equalsIgnoreCase(a.getEstado())).count();
        lblTotalRegistros.setText(String.valueOf(total));
        lblTotalPresentes.setText(String.valueOf(presentes));
        lblTotalTarde.setText(String.valueOf(tardes));
        if (lblTotalFaltas != null) lblTotalFaltas.setText(String.valueOf(faltas));
    }

    private void mostrarAlerta(String t, String m, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m);
        a.showAndWait();
    }
}