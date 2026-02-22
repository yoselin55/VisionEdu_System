# 🎓 VisionEdu - Sistema Biométrico de Control de Asistencia

Sistema de control de asistencia escolar mediante reconocimiento facial,
desarrollado en Java con JavaFX y OpenCV.

---

## 📋 Requisitos

Antes de ejecutar el proyecto necesitas tener instalado:

- [Java JDK 17+](https://www.oracle.com/java/technologies/downloads/)
- [MySQL 8.0+](https://dev.mysql.com/downloads/mysql/)
- [Maven](https://maven.apache.org/download.cgi)
- Una cámara web conectada

---

## 🗄️ Configurar la Base de Datos

### 1. Abre MySQL Workbench o cualquier cliente MySQL

### 2. Ejecuta el script de la base de datos
```sql
source database/schema.sql
```

O abre el archivo `database/schema.sql` en tu cliente MySQL
y ejecútalo completo.

Esto creará automáticamente:
- La base de datos `visionedu`
- Las tablas `alumnos`, `asistencias`, `usuarios`
- Un usuario administrador por defecto

### 3. Credenciales del usuario por defecto

| Campo    | Valor    |
|----------|----------|
| Usuario  | admi     |
| Password | 12345678 |

---

## ⚙️ Configurar la Conexión

Abre el archivo `src/config/Conexion.java` y cambia
los datos según tu instalación de MySQL:
```java
private static final String URL  = "jdbc:mysql://localhost:3306/visionedu";
private static final String USER = "root";        // tu usuario de MySQL
private static final String PASS = "";            // tu contraseña de MySQL
```

---

## ▶️ Ejecutar el Proyecto

### Desde IntelliJ IDEA
1. Abre el proyecto
2. Espera que Maven descargue las dependencias
3. Ejecuta la clase `main/Launcher.java`

### Desde terminal
```bash
mvn javafx:run
```

---

## 📁 Estructura del Proyecto
```
VisionEdu/
├── src/
│   ├── config/          # Conexión a base de datos
│   ├── controller/      # Lógica de cada pantalla
│   ├── dao/             # Acceso a base de datos
│   ├── data/            # Reconocimiento facial (OpenCV)
│   │   └── images/      # Fotos biométricas (no incluidas)
│   ├── main/            # Punto de entrada
│   ├── models/          # Entidades (Alumno, Asistencia, Usuario)
│   ├── services/        # Generación de reportes
│   ├── utils/           # Tema e idiomas
│   └── view/            # Interfaces FXML
├── database/
│   └── schema.sql       # Script de base de datos
└── README.md
```

---

## 🌐 Idiomas Disponibles

El sistema soporta 3 idiomas seleccionables desde el menú principal:
- 🇪🇸 Español
- 🇺🇸 English
- 🇧🇷 Português

---

## ⚠️ Notas Importantes

- Las fotos biométricas se guardan localmente en `src/data/images/`
  y **no se suben al repositorio** por privacidad
- El modelo de reconocimiento facial se entrena automáticamente
  al registrar alumnos
- Se requiere cámara web para el módulo de Control de Acceso

---

## 🛠️ Tecnologías

- Java 17
- JavaFX 17
- OpenCV (JavaCV)
- MySQL 8
- Apache POI (exportar Excel)
- iText (exportar PDF)
- Maven