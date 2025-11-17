# 🐳 Docker Setup - Secret Santa

## Quick Start

### Using Docker Compose (Recommended)

```bash
# 1. Copy environment variables
cp .env.example .env

# 2. Edit .env with your email credentials
nano .env

# 3. Start all services
docker-compose up -d

# 4. Check logs
docker-compose logs -f app

# 5. Access application
open http://localhost:8080
```

### Manual Docker Build

```bash
# Build image
docker build -t secretsanta:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  --name secretsanta \
  -e POSTGRES_URL=jdbc:postgresql://host.docker.internal:5432/secretsanta \
  -e POSTGRES_API_USER=postgres \
  -e POSTGRES_API_PASSWORD=postgrespassword \
  -e MAIL_USERNAME=your-email@gmail.com \
  -e MAIL_PASSWORD=your-app-password \
  secretsanta:latest
```

## Best Practices Implemented

### 🔒 Security
- ✅ Non-root user (uid 1000)
- ✅ Minimal Alpine base image
- ✅ Multi-stage build (smaller final image)
- ✅ Security scanning with Trivy
- ✅ SBOM generation
- ✅ No secrets in image

### 🚀 Performance
- ✅ Layer caching optimization
- ✅ Spring Boot layered jars
- ✅ JVM container-aware settings
- ✅ G1GC garbage collector
- ✅ Resource limits defined

### 📦 Image Optimization
- ✅ Multi-platform support (amd64/arm64)
- ✅ Efficient layer ordering
- ✅ .dockerignore to reduce context
- ✅ Health checks configured
- ✅ Proper labels (OCI standards)

### 🔄 CI/CD
- ✅ Automated builds on push
- ✅ Container registry integration (GHCR)
- ✅ Semantic versioning
- ✅ Build cache enabled
- ✅ Vulnerability scanning

## Image Details

**Base Images:**
- Build: `gradle:8.11.1-jdk25-alpine`
- Runtime: `eclipse-temurin:25-jre-alpine`

**Image Size:** ~300MB (optimized with layers)

**Exposed Ports:**
- 8080 - Application HTTP port

**Health Check:**
- Endpoint: `/actuator/health` (Spring Boot Actuator)
- Interval: 30s
- Timeout: 10s
- Retries: 3

**Additional Actuator Endpoints:**
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

## Environment Variables

### Required
```bash
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### Optional
```bash
SPRING_PROFILES_ACTIVE=prod
POSTGRES_URL=jdbc:postgresql://postgres:5432/secretsanta
POSTGRES_API_USER=postgres
POSTGRES_API_PASSWORD=postgrespassword
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_FROM=noreply@secretsanta.com
APP_BASE_URL=http://localhost:8080
```

## Docker Compose Services

### Application (app)
- Spring Boot application
- Auto-restart on failure
- Health checks enabled
- Resource limits: 1GB RAM, 2 CPUs

### PostgreSQL (postgres)
- PostgreSQL 16 Alpine
- Data persistence with volumes
- Health checks configured
- Resource limits: 512MB RAM, 1 CPU

### PgAdmin (pgadmin) - Optional
- Web-based database admin
- Access: http://localhost:5050
- Default credentials in docker-compose.yml

## Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f app

# Rebuild image
docker-compose build --no-cache

# Scale application (not recommended without load balancer)
docker-compose up -d --scale app=3

# Execute commands in container
docker-compose exec app sh

# Database backup
docker-compose exec postgres pg_dump -U postgres secretsanta > backup.sql

# Database restore
docker-compose exec -T postgres psql -U postgres secretsanta < backup.sql
```

## Troubleshooting

### Container won't start
```bash
# Check logs
docker-compose logs app

# Check health
docker-compose ps
```

### Database connection issues
```bash
# Verify database is ready
docker-compose exec postgres pg_isready -U postgres

# Check network
docker network ls
docker network inspect secretsanta_secretsanta-network
```

### Email not sending
```bash
# Check environment variables
docker-compose exec app env | grep MAIL

# Test email configuration
curl "http://localhost:8080/test/email?email=test@example.com"
```

## Production Deployment

### With GitHub Container Registry

```bash
# Pull from registry
docker pull ghcr.io/your-username/secretsanta:latest

# Run with production settings
docker run -d \
  -p 8080:8080 \
  --name secretsanta \
  --env-file .env.production \
  --restart unless-stopped \
  ghcr.io/your-username/secretsanta:latest
```

### Resource Recommendations

**Minimum:**
- CPU: 0.5 cores
- RAM: 512MB
- Disk: 2GB

**Recommended:**
- CPU: 1-2 cores
- RAM: 1GB
- Disk: 5GB

## Security Scanning

```bash
# Scan with Trivy
docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy:latest image secretsanta:latest

# Scan with Snyk
snyk container test secretsanta:latest
```

## Monitoring

Add to docker-compose.yml for monitoring:

```yaml
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
```

## License

See main repository LICENSE file.
