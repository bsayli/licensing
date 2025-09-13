FROM maven:3.9.11-eclipse-temurin-21-noble AS builder
WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -T 2C -DskipTests --no-transfer-progress clean package

FROM eclipse-temurin:21.0.8_9-jre-noble
WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates tzdata && \
    rm -rf /var/lib/apt/lists/* && \
    useradd -r -s /bin/false -d /app appuser && \
    mkdir -p /app/logs && chown -R appuser:appuser /app

LABEL org.opencontainers.image.title="licensing-service-sdk-cli" \
      org.opencontainers.image.description="CLI for Licensing Service SDK" \
      org.opencontainers.image.version="1.0.1" \
      org.opencontainers.image.source="https://github.com/bsayli/licensing" \
      org.opencontainers.image.licenses="Apache-2.0"

COPY --from=builder --chown=appuser:appuser /app/target/*.jar /app/licensing-service-sdk-cli.jar

USER appuser

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/licensing-service-sdk-cli.jar \"$@\"", "--"]