# =====================================================================
# Backend Scrivania Digitale ANC - Sprint 0 Foundation
# Build multi-stage: stage 1 compila con Maven, stage 2 runtime JRE 21.
# =====================================================================

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dipendenze
COPY apps/backend/pom.xml ./pom.xml
RUN mvn -B dependency:go-offline

# Sorgenti applicativi
COPY apps/backend/src ./src
RUN mvn -B -DskipTests package -e

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Required by docker-compose healthcheck (wget on /actuator/health/readiness)
RUN apt-get update \
	&& apt-get install -y --no-install-recommends wget \
	&& rm -rf /var/lib/apt/lists/*

# JAR fat
COPY --from=build /workspace/target/anc-backend-*.jar /app/app.jar

# Migration SQL versionate (source-of-truth in /infra/db/migrations).
COPY infra/db/migrations /app/infra/db/migrations
ENV ANC_FLYWAY_SQL_LOCATION=/app/infra/db/migrations

# Profilo runtime di default in Docker = poc
ENV SPRING_PROFILES_ACTIVE=poc

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
