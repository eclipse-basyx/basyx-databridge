FROM eclipse-temurin:11

# Install wget to check health status
RUN apt update && apt install -y wget && apt clean

# Copy built jar to image using the jar name specified in the pom.xml (JAR_FILE)
ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} /usr/share/databridgeExecutable.jar
COPY target/lib /usr/share/lib

# Expose this port for health check
EXPOSE 8085

# Start the jar
CMD java -jar "/usr/share/databridgeExecutable.jar"
