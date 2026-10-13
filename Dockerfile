# syntax=docker/dockerfile:1

# ---- build: compile, run the unit tests, package ---------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp -q dependency:go-offline

COPY src src
RUN ./mvnw -B -ntp package

# ---- run: JRE only, as an unprivileged user ---------------------------------
FROM eclipse-temurin:17-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --home-dir /app --shell /usr/sbin/nologin app \
    && mkdir -p /app /var/log/expense-portal \
    && chown app:app /var/log/expense-portal

WORKDIR /app
COPY --from=build /src/target/expense-portal-api.jar /app/app.jar

USER app
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
