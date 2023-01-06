FROM openjdk:11-slim-bullseye

# Copy built jar to image using the jar name specified in the pom.xml (JAR_FILE)
ARG JAR_FILE

COPY target/${JAR_FILE} /usr/share/databridgeExecutable.jar
COPY target/lib /usr/share/lib

# Start the jar
CMD java -jar "/usr/share/databridgeExecutable.jar"
