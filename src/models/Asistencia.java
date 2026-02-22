package models;

import java.sql.Date;
import java.sql.Time;

public class Asistencia {
    private int idAsistencia;
    private int idAlumno;
    private String nombreAlumno;
    private Date fecha;
    private Time hora;
    private String estado; // "Presente", "Tarde", "Ausente"

    public Asistencia() {
    }

    public Asistencia(int idAsistencia, int idAlumno, Date fecha, Time hora, String estado) {
        this.idAsistencia = idAsistencia;
        this.idAlumno = idAlumno;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    public Asistencia(int idAlumno, Date fecha, Time hora, String estado) {
        this.idAlumno = idAlumno;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(int idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public int getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(int idAlumno) {
        this.idAlumno = idAlumno;
    }

    public String getNombreAlumno() {
        return nombreAlumno;
    }

    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Time getHora() {
        return hora;
    }

    public void setHora(Time hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}