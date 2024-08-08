# Use a Java 21 base image
FROM openjdk:21-slim

RUN apt-get update && apt-get install -y maven

# Set a working directory for the application
WORKDIR /app

# Copy the project source code
COPY src/main/java ./src/main/java
COPY src/main/resources ./src/main/resources
COPY pom.xml .

# Install Maven dependencies
RUN mvn clean package

ENTRYPOINT ["java", "-jar", "target/licensing-service-sdk-cli-1.0.1.jar"]