# syntax=docker/dockerfile:1

# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew

RUN ./gradlew --no-daemon dependencies || true

COPY src/ src/

RUN ./gradlew --no-daemon clean bootJar -x test

# Runtime stage
FROM eclipse-temurin:25-jre-alpine@sha256:69c2a26e9894016e274c880fa542956d005b3755f84db765145185e87405bd89

LABEL org.opencontainers.image.title="Secret Santa"
LABEL org.opencontainers.image.description="A Secret Santa web application"
LABEL org.opencontainers.image.authors="jotxee"
LABEL org.opencontainers.image.source="https://github.com/asixc/secret-santa"

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
