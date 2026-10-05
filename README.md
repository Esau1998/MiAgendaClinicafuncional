# Mi Agenda de Clínica

Sistema de gestión de citas médicas hecho con **Java 17 + Spring Boot 3.3 + Spring Data JPA + Thymeleaf + MySQL 8**.

## Qué incluye

- **Inicio de sesión** con usuario *o* correo, cierre de sesión y cambio de cuenta sin problemas (cada ingreso crea una sesión nueva).
- **Crear cuenta** (`/registro`): validación de usuario, correo y contraseña; las cuentas nuevas nacen con rol `RECEPCIONISTA`.
- **Olvidé mi contraseña** (`/olvide-password`): enlace de un solo uso que vence a los 30 minutos. Se envía por correo si configuras SMTP; si no, se muestra en pantalla (modo desarrollo).
- **Mi perfil** (`/perfil`): editar datos y cambiar contraseña.
- **Usuarios (solo ADMINISTRADOR)**: crear, editar, activar/desactivar y eliminar. No permite borrarte a ti mismo ni dejar el sistema sin un administrador activo.
- **Catálogo público** (`/catalogo`): especialidades, médicos con sus horarios, búsqueda y filtros, y **solicitud de cita** sin iniciar sesión. Solo ofrece horas libres del médico (bloques de 30 min, hasta 90 días) y no permite choques.
- **Fondos cambiables**: botón flotante (esquina inferior derecha) con 8 fondos incluidos, subida de tu propia imagen, URL, oscurecer y desenfocar. Se guarda en tu navegador.
- Módulos: dashboard, pacientes, médicos, especialidades, horarios, citas, historial clínico, tratamientos, pagos y reportes.
- Contraseñas cifradas con **BCrypt**. Las contraseñas antiguas en texto plano se convierten solas al primer inicio.

## Cómo ejecutarlo

1. Instala **JDK 17+**, **Maven** y **MySQL 8** (Workbench 8.0 CE).
2. (Opcional) Ejecuta `database/crear_base_de_datos.sql` en Workbench. Si no, la app crea la base sola.
3. Abre `src/main/resources/application.properties` y pon tu contraseña de MySQL en `spring.datasource.password`
   (o define la variable de entorno `DB_PASSWORD`). Usuario por defecto: `root`.
4. Ejecuta `mvn spring-boot:run` (o el botón Run de IntelliJ sobre `MiAgendaClinicaApplication`).
5. Abre <http://localhost:8080>.

Si ya tenías datos en `mi_agenda_clinica`, se conservan: Hibernate solo agrega las tablas y columnas nuevas.

## Acceso inicial

- Usuario: `admin`
- Contraseña: `admin123` (cámbiala en **Mi perfil** después de entrar).

## Opciones útiles (`application.properties`)

| Propiedad | Para qué sirve |
|---|---|
| `app.registro.habilitado` | Activa o desactiva `/registro`. |
| `app.registro.activar-automaticamente` | `false` = las cuentas nuevas esperan aprobación de un administrador. |
| `app.recuperacion.mostrar-enlace` | `true` muestra el enlace de recuperación en pantalla si no hay correo (ponlo en `false` en producción). |
| `app.base-url` | Dirección pública del sistema; se usa dentro del enlace de recuperación. |
| `app.datos-demo` | Carga especialidades, médicos y horarios de ejemplo si la base está vacía. Ponlo en `false` para empezar con datos propios. |
| `spring.mail.*` | Configura SMTP para enviar el enlace de recuperación por correo (hay un ejemplo comentado). |

## Estructura

```
src/main/java/org/esfe/miagendaclinica
├── config/      SesionFiltro (acceso), WebConfig (BCrypt), DataInitializer (roles, admin, datos demo)
├── controller/  AuthController, CatalogoController, PerfilController, UsuarioController, ...
├── service/     UsuarioService, PasswordResetService, DisponibilidadService, SolicitudCitaService, ...
├── model/       Entidades JPA (incluye PasswordResetToken)
└── dto/         UsuarioForm, MedicoCatalogo
src/main/resources
├── templates/   Pantallas Thymeleaf (login, registro, catalogo, dashboard, ...)
└── static/      css, js y fondos (img/fondos/*.svg)
```

## Notas

- Para que las fotos de los médicos aparezcan en el catálogo, pega la URL de la foto en el formulario del médico; sin foto se muestran las iniciales.
- Si olvidas la clave del administrador y no hay correo configurado, usa la consulta comentada al final de `database/crear_base_de_datos.sql`.
