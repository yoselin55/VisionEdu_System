package dao;

import config.Conexion;
import models.Asistencia;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaDAO {

    public void registrarFaltasAutomaticas() {
        new Thread(() -> {
            try {
                LocalDate hoy = LocalDate.now();
                DayOfWeek diaSemana = hoy.getDayOfWeek();
                if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
                    System.out.println("Fin de semana, no se registran faltas automaticas");
                    return;
                }
                String sqlAlumnos   = "SELECT id_alumno FROM alumnos";
                String sqlVerificar = "SELECT COUNT(*) FROM asistencias WHERE id_alumno = ? AND fecha = ?";
                try (Connection conn = Conexion.getConnection()) {
                    List<Integer> ids = new ArrayList<>();
                    try (PreparedStatement ps = conn.prepareStatement(sqlAlumnos);
                         ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) ids.add(rs.getInt("id_alumno"));
                    }
                    for (int idAlumno : ids) {
                        try (PreparedStatement psV = conn.prepareStatement(sqlVerificar)) {
                            psV.setInt(1, idAlumno);
                            psV.setDate(2, Date.valueOf(hoy));
                            try (ResultSet rs = psV.executeQuery()) {
                                if (rs.next() && rs.getInt(1) == 0)
                                    System.out.println("Alumno " + idAlumno + " pendiente hoy");
                            }
                        }
                    }
                    System.out.println("Verificacion diaria completada para " + hoy);
                }
            } catch (Exception e) {
                System.err.println("Error en registro automatico: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public void cerrarDiaAnterior() {
        new Thread(() -> {
            try {
                LocalDate ayer = LocalDate.now().minusDays(1);
                DayOfWeek dia = ayer.getDayOfWeek();
                if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) return;

                String sql = "INSERT INTO asistencias (id_alumno, fecha, hora, estado) " +
                        "SELECT a.id_alumno, ?, '23:59:59', 'Falta' " +
                        "FROM alumnos a " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM asistencias ast " +
                        "  WHERE ast.id_alumno = a.id_alumno AND ast.fecha = ?" +
                        ")";
                try (Connection conn = Conexion.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDate(1, Date.valueOf(ayer));
                    ps.setDate(2, Date.valueOf(ayer));
                    int filas = ps.executeUpdate();
                    System.out.println("Faltas registradas para " + ayer + ": " + filas);
                }
            } catch (Exception e) {
                System.err.println("Error cerrando dia: " + e.getMessage());
            }
        }).start();
    }

    public boolean registrarAsistencia(Asistencia asistencia) {
        String sql = "INSERT INTO asistencias (id_alumno, fecha, hora, estado) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, asistencia.getIdAlumno());
            ps.setDate(2, asistencia.getFecha());
            ps.setTime(3, asistencia.getHora());
            ps.setString(4, asistencia.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean verificarAsistenciaHoy(int idAlumno) {
        String sql = "SELECT COUNT(*) FROM asistencias WHERE id_alumno = ? AND fecha = CURDATE()";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Asistencia> filtrarPorFecha(Date fecha) {
        List<Asistencia> lista = new ArrayList<>();
        // ← al.grado agregado al SELECT
        String sql = "SELECT al.id_alumno, al.grado, " +
                "CONCAT(al.nombre, ' ', al.apellido) AS nombre_alumno, " +
                "a.id_asistencia, IFNULL(a.fecha, ?) AS fecha_ref, a.hora, " +
                "CASE " +
                "  WHEN a.estado IS NOT NULL THEN a.estado " +
                "  WHEN ? < CURDATE() THEN 'Falta' " +
                "  ELSE 'Pendiente' " +
                "END AS estado_dinamico " +
                "FROM alumnos al " +
                "LEFT JOIN asistencias a ON al.id_alumno = a.id_alumno AND a.fecha = ? " +
                "ORDER BY nombre_alumno ASC";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, fecha);
            ps.setDate(2, fecha);
            ps.setDate(3, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Asistencia a = new Asistencia();
                    a.setIdAsistencia(rs.getInt("id_asistencia"));
                    a.setIdAlumno(rs.getInt("id_alumno"));
                    a.setNombreAlumno(rs.getString("nombre_alumno"));
                    a.setGrado(rs.getString("grado")); // ← nuevo
                    a.setFecha(rs.getDate("fecha_ref"));
                    a.setHora(rs.getTime("hora"));
                    a.setEstado(rs.getString("estado_dinamico"));
                    lista.add(a);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<Asistencia> listarAsistencias() {
        List<Asistencia> lista = new ArrayList<>();
        // ← al.grado agregado al SELECT
        String sql = "SELECT a.id_asistencia, a.id_alumno, al.grado, " +
                "CONCAT(al.nombre, ' ', al.apellido) AS nombre_alumno, " +
                "a.fecha, a.hora, a.estado " +
                "FROM asistencias a " +
                "INNER JOIN alumnos al ON a.id_alumno = al.id_alumno " +
                "ORDER BY a.fecha DESC, a.hora DESC";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Asistencia a = new Asistencia();
                a.setIdAsistencia(rs.getInt("id_asistencia"));
                a.setIdAlumno(rs.getInt("id_alumno"));
                a.setNombreAlumno(rs.getString("nombre_alumno"));
                a.setGrado(rs.getString("grado")); // ← nuevo
                a.setFecha(rs.getDate("fecha"));
                a.setHora(rs.getTime("hora"));
                a.setEstado(rs.getString("estado"));
                lista.add(a);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<Asistencia> filtrarPorAlumno(int idAlumno) {
        List<Asistencia> lista = new ArrayList<>();
        // ← al.grado agregado al SELECT
        String sql = "SELECT a.id_asistencia, a.id_alumno, al.grado, " +
                "CONCAT(al.nombre, ' ', al.apellido) AS nombre_alumno, " +
                "a.fecha, a.hora, a.estado " +
                "FROM asistencias a " +
                "INNER JOIN alumnos al ON a.id_alumno = al.id_alumno " +
                "WHERE a.id_alumno = ? ORDER BY a.fecha DESC";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Asistencia a = new Asistencia();
                    a.setIdAsistencia(rs.getInt("id_asistencia"));
                    a.setIdAlumno(rs.getInt("id_alumno"));
                    a.setNombreAlumno(rs.getString("nombre_alumno"));
                    a.setGrado(rs.getString("grado")); // ← nuevo
                    a.setFecha(rs.getDate("fecha"));
                    a.setHora(rs.getTime("hora"));
                    a.setEstado(rs.getString("estado"));
                    lista.add(a);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}