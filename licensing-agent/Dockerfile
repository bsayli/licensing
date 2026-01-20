FROM maven:3.9.11-eclipse-temurin-21-noble AS builder
WORKDIR /build

COPY licensing-service-client/pom.xml licensing-service-client/pom.xml
COPY licensing-service-sdk/pom.xml     licensing-service-sdk/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests -f licensing-service-client/pom.xml dependency:go-offline

COPY licensing-service-client/ licensing-service-client/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests -f licensing-service-client/pom.xml install

COPY licensing-service-sdk/ licensing-service-sdk/

RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests -f licensing-service-sdk/pom.xml dependency:go-offline && \
    mvn -q -T 2C --no-transfer-progress -DskipTests -f licensing-service-sdk/pom.xml clean package && \
    java -Djarmode=layertools -jar licensing-service-sdk/target/*.jar extract

FROM eclipse-temurin:21.0.8_9-jre-noble
WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates curl tzdata && \
    rm -rf /var/lib/apt/lists/* && \
    useradd -r -s /bin/false -d /app appuser && \
    mkdir -p /app/logs && chown -R appuser:appuser /app

LABEL org.opencontainers.image.title="licensing-service-sdk" \
      org.opencontainers.image.description="Licensing Service SDK (Spring Boot)" \
      org.opencontainers.image.version="1.0.1" \
      org.opencontainers.image.source="https://github.com/bsayli/licensing" \
      org.opencontainers.image.licenses="MIT"

COPY --from=builder --chown=appuser:appuser /build/dependencies/          ./
COPY --from=builder --chown=appuser:appuser /build/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appuser /build/spring-boot-loader/    ./
COPY --from=builder --chown=appuser:appuser /build/application/           ./

USER appuser

ENV SPRING_PROFILES_ACTIVE=dev \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8"

EXPOSE 8082

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -cp /app org.springframework.boot.loader.launch.JarLauncher"]