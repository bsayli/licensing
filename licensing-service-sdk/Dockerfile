FROM maven:3.9.8-amazoncorretto-21-al2023 AS builder
WORKDIR /app
COPY src/main/java ./src/main/java
COPY src/main/resources ./src/main/resources
COPY pom.xml .
RUN mvn clean package

FROM eclipse-temurin:21-jre-ubi9-minimal
WORKDIR /app
COPY --from=builder /app/target/*.jar licensing-service-sdk.jar
EXPOSE 8082
CMD ["java", "-jar", "licensing-service-sdk.jar"]