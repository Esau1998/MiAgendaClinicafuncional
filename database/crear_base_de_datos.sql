-- ============================================================
-- Mi Agenda de Clínica - Script para MySQL Workbench 8.0 CE
-- ============================================================
-- La aplicación crea y actualiza las tablas SOLA al iniciar
-- (spring.jpa.hibernate.ddl-auto=update), sin borrar tus datos.
-- Solo necesitas que exista la base de datos:

CREATE DATABASE IF NOT EXISTS mi_agenda_clinica
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE mi_agenda_clinica;

-- ------------------------------------------------------------
-- Consultas útiles (ejecútalas DESPUÉS de iniciar la app una vez)
-- ------------------------------------------------------------

-- Ver los usuarios registrados
-- SELECT u.id, u.username, u.email, u.nombre_completo, r.nombre AS rol, u.activo
--   FROM usuarios u JOIN roles r ON r.id = u.rol_id;

-- Ver las citas solicitadas desde el catálogo público
-- SELECT c.id, c.fecha, c.hora, c.estado, p.nombre, p.apellido, c.observaciones
--   FROM citas c JOIN pacientes p ON p.id = c.paciente_id
--  WHERE c.observaciones LIKE 'Solicitada desde el catálogo%'
--  ORDER BY c.fecha, c.hora;

-- ------------------------------------------------------------
-- Recuperar el acceso del administrador (restablece la clave a: admin123)
-- Descomenta, ejecuta y luego cambia la contraseña desde "Mi perfil".
-- ------------------------------------------------------------
-- UPDATE usuarios
--    SET password = '$2b$10$J0utU8a2zXDw0hErig63bOiOzUPecSGExNV3sulG71hLYA6IzFG.u',
--        activo = 1
--  WHERE username = 'admin';
