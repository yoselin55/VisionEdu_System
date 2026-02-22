CREATE DATABASE IF NOT EXISTS visionedu
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE visionedu;

-- ── TABLA ALUMNOS ────────────────────────────
CREATE TABLE IF NOT EXISTS alumnos (
    id_alumno      INT(11)      NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(100) NOT NULL,
    apellido       VARCHAR(100) NOT NULL,
    dni            VARCHAR(8)   NOT NULL,
    grado          VARCHAR(50)  NOT NULL,
    foto_path      VARCHAR(255) DEFAULT NULL,
    fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_alumno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ── TABLA ASISTENCIAS ────────────────────────
CREATE TABLE IF NOT EXISTS asistencias (
    id_asistencia  INT(11)     NOT NULL AUTO_INCREMENT,
    id_alumno      INT(11)     DEFAULT NULL,
    fecha          DATE        DEFAULT (CURDATE()),
    hora           TIME        DEFAULT NULL,
    estado         VARCHAR(20) DEFAULT 'Presente',
    PRIMARY KEY (id_asistencia),
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id_alumno)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ── TABLA USUARIOS ───────────────────────────
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario      INT(11)      NOT NULL AUTO_INCREMENT,
    nombre_usuario  VARCHAR(50)  NOT NULL,
    password        VARCHAR(255) NOT NULL,
    rol             VARCHAR(20)  DEFAULT 'admin',
    PRIMARY KEY (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ── DATOS INICIALES ──────────────────────────
INSERT INTO usuarios (nombre_usuario, password, rol)
VALUES ('admi', '12345678', 'admin');
