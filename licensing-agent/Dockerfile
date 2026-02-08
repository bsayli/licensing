FROM maven:3.9.12-eclipse-temurin-21-noble AS builder
WORKDIR /app

COPY licensing-service-client/pom.xml licensing-service-client/pom.xml
COPY licensing-agent/pom.xml         licensing-agent/pom.xml

RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -q -DskipTests --no-transfer-progress -f licensing-service-client/pom.xml dependency:go-offline

COPY licensing-service-client/ licensing-service-client/
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -q -DskipTests --no-transfer-progress -f licensing-service-client/pom.xml install

COPY licensing-agent/ licensing-agent/

WORKDIR /app/licensing-agent
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -q -DskipTests --no-transfer-progress dependency:go-offline && \
    mvn -q -T 2C -DskipTests --no-transfer-progress clean package && \
    java -Djarmode=layertools -jar target/licensing-agent.jar extract


FROM eclipse-temurin:21.0.10_7-jre-noble
WORKDIR /app

RUN useradd -r -u 10001 -g root -s /bin/false -d /app appuser && \
    mkdir -p /app/logs && \
    chown -R 10001:0 /app

LABEL org.opencontainers.image.title="licensing-agent" \
      org.opencontainers.image.description="Licensing Agent" \
      org.opencontainers.image.source="https://github.com/bsayli/licensing" \
      org.opencontainers.image.licenses="MIT"

COPY --from=builder --chown=10001:0 /app/licensing-agent/dependencies/ ./
COPY --from=builder --chown=10001:0 /app/licensing-agent/snapshot-dependencies/ ./
COPY --from=builder --chown=10001:0 /app/licensing-agent/spring-boot-loader/ ./
COPY --from=builder --chown=10001:0 /app/licensing-agent/application/ ./

USER 10001

ENV SPRING_PROFILES_ACTIVE=dev \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8"

EXPOSE 8082

ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]