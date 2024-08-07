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
RUN mvn clean install

# Expose the application port (replace 8080 with your actual port)
EXPOSE 8082

# Specify the command to run the application (replace "your.main.class" with your actual main class)
CMD ["java", "-jar", "target/licensing-service-sdk-1.0.1.jar"]