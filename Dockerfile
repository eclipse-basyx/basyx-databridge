# Stage 1: Build the application
FROM maven:3.8.1-openjdk-11-slim AS build

WORKDIR /workspace

# Copy the source code and build the application
COPY . .
RUN mvn clean package

# Stage 2: Create the final executable docker image
FROM openjdk:11-slim-bullseye

# Install wget to check health status
RUN apt update && apt install -y wget && apt clean

# Copy the built jar file from the build stage
COPY --from=build /workspace/target/*.jar /usr/share/databridgeExecutable.jar
COPY --from=build /workspace/target/lib /usr/share/lib

# Expose this port for health check 
EXPOSE 8085

# Start the jar
CMD java -jar "/usr/share/databridgeExecutable.jar"
