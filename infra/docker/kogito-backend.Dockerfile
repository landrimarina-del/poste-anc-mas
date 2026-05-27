# =====================================================================
# Backend Scrivania Digitale ANC - Sprint 0 Foundation
# Build single-stage: copia il JAR pre-buildato localmente con Maven.
# Il build locale garantisce che kogito-maven-plugin generi ConfigBean
# a partire dai file BPMN/DMN presenti nel progetto.
#
# PRE-REQUISITO: eseguire prima:
#   cd apps/kogito/backend && mvn -DskipTests package
# =====================================================================

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Required by docker-compose healthcheck (wget on /actuator/health/readiness)
RUN apt-get update \
	&& apt-get install -y --no-install-recommends wget \
	&& rm -rf /var/lib/apt/lists/*

# JAR fat pre-buildato localmente (include ConfigBean generato da kogito-maven-plugin)
COPY apps/kogito/backend/target/anc-backend-*.jar /app/app.jar

# Migration SQL versionate (source-of-truth in /infra/db/migrations).
COPY infra/db/migrations /app/infra/db/migrations
ENV ANC_FLYWAY_SQL_LOCATION=/app/infra/db/migrations

# Profilo runtime di default in Docker = poc
ENV SPRING_PROFILES_ACTIVE=poc

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

