# syntax=docker/dockerfile:1

# Build stage (Alpine, single-arch)
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew

RUN ./gradlew --no-daemon dependencies || true

COPY src/ src/

RUN ./gradlew --no-daemon clean bootJar -x test

# Runtime stage (Alpine JRE)
FROM eclipse-temurin:25-jre-alpine

LABEL org.opencontainers.image.title="Secret Santa"
LABEL org.opencontainers.image.description="A Secret Santa web application"
LABEL org.opencontainers.image.authors="jotxee"
LABEL org.opencontainers.image.source="https://github.com/jotxee/secretsanta"

RUN addgroup -S appuser && adduser -S appuser -G appuser

WORKDIR /app

ENV JAVA_OPTS=""

COPY --from=build /app/build/libs/*-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
