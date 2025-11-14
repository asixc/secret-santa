-- Script de inicialización para Secret Santa Database
-- Este script se ejecuta automáticamente cuando se crea el contenedor

-- Crear extensión para UUID (opcional, si quieres usar UUID nativos)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Las tablas se crearán automáticamente por Hibernate con ddl-auto: update
-- Este archivo está aquí por si quieres agregar datos de prueba o configuraciones extras

-- Ejemplo de datos de prueba (descomenta si quieres usarlos)
/*
INSERT INTO sorteos (nombre, fecha_creacion, activo) VALUES
  ('Amigo Invisible Familia 2025', NOW(), true),
  ('Secret Santa Oficina', NOW(), true);

INSERT INTO participantes (sorteo_id, nombre, email, token, asignado_a) VALUES
  (1, 'Juan', 'juan@ejemplo.com', uuid_generate_v4()::text, 'María'),
  (1, 'María', 'maria@ejemplo.com', uuid_generate_v4()::text, 'Pedro'),
  (1, 'Pedro', 'pedro@ejemplo.com', uuid_generate_v4()::text, 'Juan');
*/

-- Confirmar creación
SELECT 'Base de datos Secret Santa inicializada correctamente' AS mensaje;
