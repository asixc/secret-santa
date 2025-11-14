# 🎄 Secret Santa - Amigo Invisible

Aplicación web para gestionar sorteos de amigo invisible con asignaciones aleatorias y envío automático de emails.

## 🚀 Inicio Rápido

### 1. Levantar la base de datos

```bash
# Levantar PostgreSQL y pgAdmin
docker-compose up -d

# Ver logs
docker-compose logs -f postgres

# Verificar que está funcionando
docker-compose ps
```

### 2. Configurar variables de entorno

```bash
# Copiar el template
cp .env.example .env

# Editar con tus credenciales de email
nano .env
```

### 3. Ejecutar la aplicación

```bash
# Con Gradle
./gradlew bootRun

# O generar el JAR y ejecutarlo
./gradlew build -x test
java -jar build/libs/secretsanta-0.0.1-SNAPSHOT.jar
```

La aplicación estará disponible en: **http://localhost:8080**

## 🗄️ Servicios Docker

| Servicio | Puerto | Acceso |
|----------|--------|--------|
| PostgreSQL | 5432 | `jdbc:postgresql://localhost:5432/secretsanta` |
| pgAdmin | 5050 | http://localhost:5050 (admin@secretsanta.com / admin) |

### Conectar pgAdmin a PostgreSQL

1. Abre http://localhost:5050
2. Login: `admin@secretsanta.com` / `admin`
3. Add New Server:
   - **Name:** Secret Santa
   - **Host:** postgres (nombre del servicio en docker-compose)
   - **Port:** 5432
   - **Username:** postgres
   - **Password:** postgres

## 📧 Configuración de Email (Gmail)

1. Ve a tu cuenta de Google → Seguridad
2. Activa "Verificación en 2 pasos"
3. Genera una "Contraseña de aplicación"
4. Usa esa contraseña en `MAIL_PASSWORD` del archivo `.env`

## 🛠️ Comandos útiles

```bash
# Detener contenedores
docker-compose down

# Detener y eliminar datos (⚠️ borra la DB)
docker-compose down -v

# Ver logs de PostgreSQL
docker-compose logs -f postgres

# Reiniciar solo PostgreSQL
docker-compose restart postgres

# Acceder a psql
docker exec -it secretsanta-postgres psql -U postgres -d secretsanta
```

## 📁 Estructura del Proyecto

```
secretsanta/
├── src/main/
│   ├── java/dev/jotxee/secretsanta/
│   │   ├── controller/     # IndexController, CreateController
│   │   ├── service/        # SecretSantaService, EmailService
│   │   ├── repository/     # SorteoRepository, ParticipanteRepository
│   │   ├── entity/         # Sorteo, Participante
│   │   └── dto/            # RevealDTO
│   ├── jte/                # Templates JTE
│   │   ├── index.jte       # Página de reveal con slot machine
│   │   └── create.jte      # Panel de administración
│   └── resources/
│       ├── static/         # CSS, JS, audio
│       └── application.yaml
├── docker-compose.yml
├── init.sql
└── .env.example
```

## 🎯 Funcionalidades

- ✅ Slot machine animado con tema navideño
- ✅ Árbol de Navidad con luces animadas
- ✅ Música de fondo (Michael Bublé)
- ✅ Confetti celebration
- ⏳ Panel de administración para crear sorteos
- ⏳ Asignación aleatoria de participantes
- ⏳ Envío automático de emails con links únicos
- ⏳ Edición de emails y reenvío

## 🐛 Troubleshooting

**Error: "port 5432 already in use"**
```bash
# Detén cualquier PostgreSQL local
brew services stop postgresql@14  # macOS
sudo systemctl stop postgresql    # Linux
```

**Error: "connection refused"**
```bash
# Verifica que el contenedor está corriendo
docker-compose ps
docker-compose logs postgres
```

**Las tablas no se crean**
- Verifica que `spring.jpa.hibernate.ddl-auto: update` en `application.yaml`
- Revisa los logs de la aplicación: `./gradlew bootRun`

---

**Desarrollado con ❤️ por Miriam & Jotxee** 🎅🎄
