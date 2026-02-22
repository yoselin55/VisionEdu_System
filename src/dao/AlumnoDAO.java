package dao;

import config.Conexion;
import models.Alumno;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlumnoDAO {

    public boolean registrarAlumno(Alumno alumno) {
        String sql = "INSERT INTO alumnos (nombre, apellido, dni, grado, foto_path) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, alumno.getNombre());
            pstmt.setString(2, alumno.getApellido());
            pstmt.setString(3, alumno.getDni());
            pstmt.setString(4, alumno.getGrado());
            pstmt.setString(5, alumno.getFotoPath());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar alumno: " + e.getMessage());
            return false;
        }
    }

    public List<Alumno> listarAlumnos() {
        List<Alumno> alumnos = new ArrayList<>();
        String sql = "SELECT * FROM alumnos ORDER BY fecha_registro DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Alumno alumno = new Alumno();
                alumno.setIdAlumno(rs.getInt("id_alumno"));
                alumno.setNombre(rs.getString("nombre"));
                alumno.setApellido(rs.getString("apellido"));
                alumno.setDni(rs.getString("dni"));
                alumno.setGrado(rs.getString("grado"));
                alumno.setFotoPath(rs.getString("foto_path"));
                alumno.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                alumnos.add(alumno);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar alumnos: " + e.getMessage());
        }
        return alumnos;
    }

    public Alumno buscarAlumnoPorId(int idAlumno) {
        Alumno alumno = null;
        String sql = "SELECT * FROM alumnos WHERE id_alumno = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    alumno = new Alumno();
                    alumno.setIdAlumno(rs.getInt("id_alumno"));
                    alumno.setNombre(rs.getString("nombre"));
                    alumno.setApellido(rs.getString("apellido"));
                    alumno.setDni(rs.getString("dni"));
                    alumno.setGrado(rs.getString("grado"));
                    alumno.setFotoPath(rs.getString("foto_path"));
                    alumno.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar alumno por ID: " + e.getMessage());
        }
        return alumno;
    }

    public List<Alumno> buscarAlumnoPorNombre(String nombre) {
        List<Alumno> alumnos = new ArrayList<>();
        String sql = "SELECT * FROM alumnos WHERE nombre LIKE ? OR apellido LIKE ? OR dni LIKE ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String busqueda = "%" + nombre + "%";
            pstmt.setString(1, busqueda);
            pstmt.setString(2, busqueda);
            pstmt.setString(3, busqueda);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Alumno alumno = new Alumno();
                    alumno.setIdAlumno(rs.getInt("id_alumno"));
                    alumno.setNombre(rs.getString("nombre"));
                    alumno.setApellido(rs.getString("apellido"));
                    alumno.setDni(rs.getString("dni"));
                    alumno.setGrado(rs.getString("grado"));
                    alumno.setFotoPath(rs.getString("foto_path"));
                    alumno.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                    alumnos.add(alumno);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar alumnos: " + e.getMessage());
        }
        return alumnos;
    }

    public boolean actualizarAlumno(Alumno alumno) {
        String sql = "UPDATE alumnos SET nombre = ?, apellido = ?, dni = ?, grado = ?, foto_path = ? WHERE id_alumno = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, alumno.getNombre());
            pstmt.setString(2, alumno.getApellido());
            pstmt.setString(3, alumno.getDni());
            pstmt.setString(4, alumno.getGrado());
            pstmt.setString(5, alumno.getFotoPath());
            pstmt.setInt(6, alumno.getIdAlumno());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarAlumno(int idAlumno) {
        String sql = "DELETE FROM alumnos WHERE id_alumno = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existeDNI(String dni) {
        String sql = "SELECT COUNT(*) FROM alumnos WHERE dni = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dni);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar DNI: " + e.getMessage());
        }
        return false;
    }

    public boolean limpiarFotoPath(int idAlumno) {
        String sql = "UPDATE alumnos SET foto_path = '' WHERE id_alumno = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}