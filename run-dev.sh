#!/bin/bash

# Script para arrancar Secret Santa en modo desarrollo local
# Copia este archivo a run-dev.sh y ajusta las credenciales

# Credenciales de admin
export ADMIN_USER=admin
export ADMIN_PASSWORD=adminpassword

# Configuración de email (ajusta estos valores)
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=tu-email@gmail.com
export MAIL_PASSWORD=tu-app-password-aqui
export MAIL_FROM=noreply@secretsanta.com

# URL base de la aplicación
export APP_BASE_URL=http://localhost:8080

# Configuración de base de datos (si necesitas cambiarla)
# export POSTGRES_URL=jdbc:postgresql://localhost:5432/secretsanta
# export POSTGRES_API_USER=postgres
# export POSTGRES_API_PASSWORD=postgrespassword

echo "🎄 Arrancando Secret Santa..."
echo "Admin User: $ADMIN_USER"
echo "Mail Host: $MAIL_HOST"
echo ""

./gradlew bootRun

