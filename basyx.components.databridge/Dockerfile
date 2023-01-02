FROM openjdk:11-slim-bullseye

# Install dependency for wait-for-it-env.sh
RUN apt update && apt install -y jq && apt clean

RUN apt update && apt install -y wget && apt clean

# Copy built jar to image using the jar name specified in the pom.xml (JAR_FILE)
ARG JAR_FILE
COPY wait-for-it.sh /
# change EOL of .sh file to LF, so the unix container can find it
RUN ["sed", "-i", "s/\r$//", "/wait-for-it.sh"]
RUN ["chmod", "+x", "/wait-for-it.sh"]
COPY wait-for-it-env.sh /
# change EOL of .sh file to LF, so the unix container can find it
RUN ["sed", "-i", "s/\r$//", "/wait-for-it-env.sh"]
RUN ["chmod", "+x", "/wait-for-it-env.sh"]

COPY target/${JAR_FILE} /usr/share/basyxExecutable.jar
COPY target/lib /usr/share/lib
# Expose the appropriate port. In case of Tomcat, this is 8080.
ARG PORT
EXPOSE ${PORT}

# Start the jar
CMD ./wait-for-it-env.sh && java -jar "/usr/share/basyxExecutable.jar"
