# syntax=docker/dockerfile:1

# Build stage
# Usamos Temurin JDK 25 base (tag noble) y el gradle wrapper del repo
FROM eclipse-temurin:25.0.1_8-jdk-noble AS build

RUN apt-get update && apt-get install -y --no-install-recommends curl unzip bash ca-certificates && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiamos wrapper/gradle config primero para cachear dependencias
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || return 0

# Copy source code
COPY src/ src/

# Build application y extrae capas del boot jar (evita el plain jar)
RUN set -euo pipefail; \
    ./gradlew build -x test --no-daemon; \
    BOOT_JAR=$(find build/libs -maxdepth 1 -name "*-SNAPSHOT.jar" ! -name "*-plain.jar" | head -n1); \
    if [ -z "$BOOT_JAR" ]; then echo "No se encontró boot jar"; exit 1; fi; \
    java -Djarmode=tools -jar "$BOOT_JAR" extract --layers --destination build/extracted; \
    rm -rf build/tmp

# Runtime stage
FROM eclipse-temurin:25.0.1_8-jre-noble

LABEL org.opencontainers.image.title="Secret Santa"
LABEL org.opencontainers.image.description="A Secret Santa web application"
LABEL org.opencontainers.image.authors="jotxee"
LABEL org.opencontainers.image.source="https://github.com/jotxee/secretsanta"

# Install required packages and create non-root user (sin fijar UID/GID para evitar colisiones)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    tzdata \
    ca-certificates \
    && groupadd -f appuser \
    && useradd -m -g appuser -s /bin/bash appuser \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy application layers from build stage
COPY --from=build --chown=appuser:appuser /app/build/extracted/dependencies/ ./
COPY --from=build --chown=appuser:appuser /app/build/extracted/spring-boot-loader/ ./
COPY --from=build --chown=appuser:appuser /app/build/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=appuser:appuser /app/build/extracted/application/ ./

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for container
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
