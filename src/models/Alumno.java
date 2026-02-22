package models;

import java.sql.Timestamp;

public class Alumno {
    private int idAlumno;
    private String nombre;
    private String apellido;
    private String dni;
    private String grado;
    private String fotoPath;
    private Timestamp fechaRegistro;

    public Alumno() {
    }

    public Alumno(int idAlumno, String nombre, String apellido, String dni, String grado,
                  String fotoPath, Timestamp fechaRegistro) {
        this.idAlumno = idAlumno;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.grado = grado;
        this.fotoPath = fotoPath;
        this.fechaRegistro = fechaRegistro;
    }

    public Alumno(String nombre, String apellido, String dni, String grado, String fotoPath) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.grado = grado;
        this.fotoPath = fotoPath;
    }

    // Getters y Setters
    public int getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(int idAlumno) {
        this.idAlumno = idAlumno;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}