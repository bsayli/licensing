FROM maven:3.9.11-eclipse-temurin-21-noble AS builder
WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -T 2C -DskipTests --no-transfer-progress clean package && \
    java -Djarmode=layertools -jar target/*.jar extract

FROM eclipse-temurin:21.0.8_9-jre-noble
WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        tzdata && \
    rm -rf /var/lib/apt/lists/* && \
    useradd -r -s /bin/false -d /app appuser && \
    mkdir -p /app/logs && \
    chown -R appuser:appuser /app

LABEL org.opencontainers.image.title="licensing-service" \
      org.opencontainers.image.description="Licensing Service (Spring Boot, EdDSA, Caffeine, Keycloak)" \
      org.opencontainers.image.version="1.0.1" \
      org.opencontainers.image.source="https://github.com/bsayli/licensing" \
      org.opencontainers.image.licenses="MIT"

COPY --from=builder --chown=appuser:appuser /app/dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appuser /app/application/ ./

USER appuser

ENV SPRING_PROFILES_ACTIVE=default \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8"

EXPOSE 8081

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -cp /app org.springframework.boot.loader.launch.JarLauncher"]