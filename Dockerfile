# syntax=docker/dockerfile:1

# Build stage
FROM gradle:8.11.1-jdk25-alpine AS build

WORKDIR /app

# Copy gradle files first for better layer caching
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || return 0

# Copy source code
COPY src/ src/

# Build application
RUN ./gradlew build -x test --no-daemon && \
    rm -rf build/tmp && \
    java -Djarmode=tools -jar build/libs/*.jar extract --layers --destination build/extracted

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

LABEL org.opencontainers.image.title="Secret Santa"
LABEL org.opencontainers.image.description="A Secret Santa web application"
LABEL org.opencontainers.image.authors="jotxee"
LABEL org.opencontainers.image.source="https://github.com/jotxee/secretsanta"

# Install required packages and create user
RUN apk add --no-cache \
    curl \
    tzdata \
    && addgroup -g 1000 appuser \
    && adduser -D -u 1000 -G appuser appuser

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
