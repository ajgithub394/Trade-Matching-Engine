# Stage 1: Build the application using a Maven image with Java 17
FROM maven:3.8-openjdk-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project into the container
COPY . .

# Run the Maven package command to build the application JAR file
RUN mvn clean package -DskipTests

# Stage 2: Create the final, lightweight image for running the application
FROM openjdk:17-jre-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the builder stage into this final image
COPY --from=builder /app/target/*.jar ./application.jar

# This is the command that will be run when the container starts
CMD ["java", "-jar", "application.jar"]