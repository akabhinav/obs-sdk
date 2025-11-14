FROM eclipse-temurin:21-jdk-alpine

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY obs-sdk-api ./obs-sdk-api
COPY obs-sdk-core ./obs-sdk-core
COPY obs-sdk-exporters ./obs-sdk-exporters
COPY obs-sdk-examples ./obs-sdk-examples
COPY config-examples ./config-examples

# Build the project
RUN mvn clean package -DskipTests

# Set the examples directory as working directory
WORKDIR /app/obs-sdk-examples

# Default command - run the all exporters test
ENTRYPOINT ["mvn", "exec:java"]
CMD ["-Dexec.mainClass=com.observability.examples.AllExportersTest"]
