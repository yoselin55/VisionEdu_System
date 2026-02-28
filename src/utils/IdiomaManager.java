package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class IdiomaManager {

    public enum Idioma { ESPANOL, INGLES, PORTUGUES }

    private static Idioma idiomaActual = Idioma.ESPANOL;
    private static final Preferences prefs =
            Preferences.userNodeForPackage(IdiomaManager.class);

    private static final Map<String, Map<Idioma, String>> traducciones = new HashMap<>();

    static {

        agregar("app.titulo",           "VisionEdu",            "VisionEdu",           "VisionEdu");

        agregar("menu.proyecto",
                "Proyecto hecho por Yoselin Flores Quispe",
                "Project made by Yoselin Flores Quispe",
                "Projeto feito por Yoselin Flores Quispe");

        agregar("login.subtitulo",
                "Control de Asistencia Biométrico",
                "Biometric Attendance Control",
                "Controle de Presença Biométrico");
        agregar("login.boton",
                "INICIAR SESIÓN",
                "LOG IN",
                "ENTRAR");
        agregar("login.usuario",
                "Usuario",
                "Username",
                "Usuário");
        agregar("login.password",
                "Contraseña",
                "Password",
                "Senha");
        agregar("login.alerta_campos_titulo",
                "Campos Vacíos",
                "Empty Fields",
                "Campos Vazios");
        agregar("login.alerta_campos_msg",
                "Por favor complete todos los campos",
                "Please fill in all fields",
                "Por favor preencha todos os campos");
        agregar("login.alerta_error_msg",
                "Usuario o contraseña incorrectos",
                "Incorrect username or password",
                "Usuário ou senha incorretos");

        agregar("menu.gestion",         "Gestión de Alumnos",   "Student Management",  "Gestão de Alunos");
        agregar("menu.acceso",          "Control de Acceso",    "Access Control",      "Controle de Acesso");
        agregar("menu.historial",       "Historial",            "History",             "Histórico");
        agregar("menu.salir",           "Salir",                "Exit",                "Sair");
        agregar("menu.subtitulo",
                "Sistema Biometrico de Control de Asistencia",
                "Biometric Attendance Control System",
                "Sistema Biométrico de Controle de Presença");
        agregar("menu.cerrar_sesion",   "Cerrar Sesion",        "Log Out",             "Sair");

        agregar("acceso.escaneando",        "Escaneando...",            "Scanning...",              "Escaneando...");
        agregar("acceso.bienvenido",        "Bienvenido, ",             "Welcome, ",               "Bem-vindo, ");
        agregar("acceso.denegado",          "  ACCESO DENEGADO",     " ACCESS DENIED",       " ACESSO NEGADO");
        agregar("acceso.no_registrado",     "Persona no registrada",    "Person not registered",   "Pessoa não registrada");
        agregar("acceso.ya_registro",       "Ya registró asistencia hoy",
                "Already registered today",
                "Já registrou presença hoje");
        agregar("acceso.detenido",          "Detenido",                 "Stopped",                 "Parado");
        agregar("acceso.iniciar",           "Iniciar Escaneo",          "Start Scan",              "Iniciar Escaneamento");
        agregar("acceso.detener",           "Detener Escaneo",          "Stop Scan",               "Parar Escaneamento");
        agregar("acceso.camara",            "Visualización de Cámara",  "Camera View",             "Visualização da Câmera");
        agregar("acceso.estado_sistema",    "Estado del Sistema",       "System Status",           "Status do Sistema");
        agregar("acceso.hora_actual",       "Hora actual",              "Current time",            "Hora atual");
        agregar("acceso.estado",            "Estado",                   "Status",                  "Estado");
        agregar("acceso.alumno_detectado",  "Alumno detectado",         "Detected student",        "Aluno detectado");

        agregar("captura.titulo",       "CAPTURA BIOMETRICA",   "BIOMETRIC CAPTURE",   "CAPTURA BIOMÉTRICA");
        agregar("captura.iniciando",    "Iniciando camara...",  "Starting camera...",  "Iniciando câmera...");
        agregar("captura.instruccion",  "Coloque su rostro frente a la camara y presione Capturar",
                "Place your face in front of the camera and press Capture",
                "Coloque seu rosto na frente da câmera e pressione Capturar");
        agregar("captura.btn",          "Capturar Foto",        "Capture Photo",       "Capturar Foto");
        agregar("captura.cancelar",     "Cancelar",             "Cancel",              "Cancelar");
        agregar("captura.completado",   "✔ Completado",         "✔ Completed",         "✔ Concluído");
        agregar("captura.exito",        "Rostro registrado correctamente.",
                "Face registered successfully.",
                "Rosto registrado com sucesso.");

        agregar("historial.titulo",             "HISTORIAL DE ASISTENCIAS", "ATTENDANCE HISTORY",     "HISTÓRICO DE PRESENÇAS");
        agregar("historial.filtrar",            "Filtrar",                  "Filter",                 "Filtrar");
        agregar("historial.todos",              "Todos",                    "All",                    "Todos");
        agregar("historial.exportar",           "Exportar Excel",           "Export Excel",           "Exportar Excel");
        agregar("historial.volver",             "Volver",                   "Back",                   "Voltar");
        agregar("historial.fecha",              "Fecha",                    "Date",                   "Data");
        agregar("historial.buscar_alumno",      "Buscar Alumno",            "Search Student",         "Buscar Aluno");
        agregar("historial.buscar_placeholder", "Nombre o ID...",           "Name or ID...",          "Nome ou ID...");
        agregar("historial.sin_registros",      "No hay registros de asistencia",
                "No attendance records",
                "Nenhum registro de presença");
        agregar("historial.col_perfil",         "PERFIL",                   "PROFILE",                "PERFIL");
        agregar("historial.col_alumno",         "ALUMNO",                   "STUDENT",                "ALUNO");
        agregar("historial.col_dia",            "DÍA",                      "DAY",                    "DIA");
        agregar("historial.col_fecha",          "FECHA",                    "DATE",                   "DATA");
        agregar("historial.col_hora",           "HORA",                     "TIME",                   "HORA");
        agregar("historial.col_estado",         "ESTADO",                   "STATUS",                 "ESTADO");
        agregar("historial.periodo_label",      "Período:",                 "Period:",                "Período:");
        agregar("historial.formato_label",      "Formato:",                 "Format:",                "Formato:");
        agregar("historial.exportado_ok",       "Reporte guardado correctamente.\nRegistros: ",
                "Report saved successfully.\nRecords: ",
                "Relatório salvo com sucesso.\nRegistros: ");

        agregar("estado.presente",      "Presente",             "Present",             "Presente");
        agregar("estado.tarde",         "Tarde",                "Late",                "Atrasado");
        agregar("estado.falta",         "Falta",                "Absent",              "Falta");

        agregar("stat.total",           "TOTAL",                "TOTAL",               "TOTAL");
        agregar("stat.presentes",       "PRESENTES",            "PRESENT",             "PRESENTES");
        agregar("stat.tardanzas",       "TARDANZAS",            "LATE",                "ATRASOS");
        agregar("stat.faltas",          "FALTAS",               "ABSENCES",            "FALTAS");

        agregar("dia.lunes",            "Lunes",                "Monday",              "Segunda");
        agregar("dia.martes",           "Martes",               "Tuesday",             "Terça");
        agregar("dia.miercoles",        "Miércoles",            "Wednesday",           "Quarta");
        agregar("dia.jueves",           "Jueves",               "Thursday",            "Quinta");
        agregar("dia.viernes",          "Viernes",              "Friday",              "Sexta");
        agregar("dia.sabado",           "Sábado",               "Saturday",            "Sábado");
        agregar("dia.domingo",          "Domingo",              "Sunday",              "Domingo");

        agregar("btn.volver",           "Volver",               "Back",                "Voltar");
        agregar("btn.guardar",          "Guardar",              "Save",                "Salvar");
        agregar("btn.cancelar",         "Cancelar",             "Cancel",              "Cancelar");
        agregar("btn.exportar_guardar", "  Exportar y Guardar","  Export & Save",    "  Exportar e Salvar");

        agregar("dialogo.periodo",      "Seleccionar Período de Exportación",
                "Select Export Period",
                "Selecionar Período de Exportação");
        agregar("periodo.todos",        "Todo el historial",    "Full history",        "Histórico completo");
        agregar("periodo.anio",         "Por año",              "By year",             "Por ano");
        agregar("periodo.mes",          "Por mes",              "By month",            "Por mês");
        agregar("periodo.semana",       "Por semana",           "By week",             "Por semana");
        agregar("voz.bienvenido",       "Bienvenido ",          "Welcome ",            "Bem-vindo ");
        agregar("voz.estado",           ". Estado ",            ". Status ",           ". Estado ");
        agregar("voz.denegado",         "Acceso denegado. Persona no registrada.",
                "Access denied. Person not registered.",
                "Acesso negada. Pessoa não registrada.");
        agregar("voz.ya_registro",      "Ya registraste tu asistencia hoy",
                "You already registered your attendance today",
                "Você já registrou sua presença hoje");

        agregar("gestion.titulo",              "GESTIÓN DE ALUMNOS",   "STUDENT MANAGEMENT",   "GESTÃO DE ALUNOS");
        agregar("gestion.datos",               "Datos del Alumno",     "Student Data",         "Dados do Aluno");
        agregar("gestion.registrar",           "Registrar",            "Register",             "Registrar");
        agregar("gestion.capturar",            "Capturar",             "Capture",              "Capturar");
        agregar("gestion.actualizar",          "Actualizar",           "Update",               "Atualizar");
        agregar("gestion.eliminar",            "Eliminar",             "Delete",               "Excluir");
        agregar("gestion.buscar",              "Buscar",               "Search",               "Buscar");
        agregar("gestion.fecha_registro",      "Fecha Registro",       "Registration Date",    "Data de Registro");
        agregar("gestion.digitos",             "dígitos",              "digits",               "dígitos");
        agregar("gestion.buscar_placeholder",  "Buscar por nombre o DNI...",
                "Search by name or ID...",
                "Buscar por nome ou CPF...");
        agregar("gestion.seleccionar_grado",   "Seleccionar Grado",    "Select Grade",         "Selecionar Turma");
        agregar("gestion.dni_existente",       "DNI ya registrado",    "ID already registered","CPF já registrado");
        agregar("gestion.capturar_ahora",      "Alumno registrado. ¿Desea capturar el rostro ahora?",
                "Student registered. Capture face now?",
                "Aluno registrado. Deseja capturar o rosto agora?");
        agregar("gestion.seleccionar_primero", "Seleccione un alumno primero",
                "Select a student first",
                "Selecione um aluno primeiro");
        agregar("gestion.registro_bloqueado",  "Registro Bloqueado",   "Registration Blocked", "Registro Bloqueado");
        agregar("gestion.ya_tiene_biometrico", "Este alumno ya posee un perfil biométrico registrado.",
                "This student already has a registered biometric profile.",
                "Este aluno já possui um perfil biométrico registrado.");
        agregar("gestion.confirmar_eliminar",  "¿Desea eliminar a",    "Delete student",       "Excluir aluno");
        agregar("gestion.eliminado_ok",        "Alumno y datos biométricos eliminados correctamente.",
                "Student and biometric data deleted successfully.",
                "Aluno e dados biométricos excluídos com sucesso.");
        agregar("gestion.campos_invalidos",    "Complete todos los campos correctamente.\nEl DNI debe tener 8 dígitos.",
                "Fill all fields correctly.\nID must be 8 digits.",
                "Preencha todos os campos.\nCPF deve ter 8 dígitos.");

        agregar("alerta.error",         "Error",                "Error",               "Erro");
        agregar("alerta.exito",         "Éxito",                "Success",             "Sucesso");

        agregar("alumno.nombre",        "Nombre",               "Name",                "Nome");
        agregar("alumno.apellido",      "Apellido",             "Last Name",           "Sobrenome");
        agregar("alumno.dni",           "DNI",                  "ID",                  "CPF");
        agregar("alumno.grado",         "Grado",                "Grade",               "Turma");
    }

    private static void agregar(String clave, String es, String en, String pt) {
        Map<Idioma, String> mapa = new HashMap<>();
        mapa.put(Idioma.ESPANOL,   es);
        mapa.put(Idioma.INGLES,    en);
        mapa.put(Idioma.PORTUGUES, pt);
        traducciones.put(clave, mapa);
    }

    public static String t(String clave) {
        Map<Idioma, String> mapa = traducciones.get(clave);
        if (mapa == null) return clave;
        return mapa.getOrDefault(idiomaActual, clave);
    }

    public static Idioma getIdioma()  { return idiomaActual; }

    public static void setIdioma(Idioma idioma) {
        idiomaActual = idioma;
        prefs.put("idioma", idioma.name());
    }

    public static void cargarIdiomaSalvado() {
        String guardado = prefs.get("idioma", Idioma.ESPANOL.name());
        try {
            idiomaActual = Idioma.valueOf(guardado);
        } catch (Exception e) {
            idiomaActual = Idioma.ESPANOL;
        }
    }

    public static String getNombreIdioma() {
        return switch (idiomaActual) {
            case ESPANOL   -> "Español";
            case INGLES    -> "English";
            case PORTUGUES -> "Português";
        };
    }
}