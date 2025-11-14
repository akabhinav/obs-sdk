FROM eclipse-temurin:21-jdk-alpine

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy parent pom
COPY ../pom.xml /app/pom.xml

# Copy all modules
COPY ../obs-sdk-api /app/obs-sdk-api
COPY ../obs-sdk-core /app/obs-sdk-core
COPY ../obs-sdk-exporters /app/obs-sdk-exporters
COPY ../obs-sdk-examples /app/obs-sdk-examples

# Build the project
RUN mvn clean package -DskipTests

# Set the examples directory as working directory
WORKDIR /app/obs-sdk-examples

# Default command - run the comprehensive test
CMD ["mvn", "exec:java", "-Dexec.mainClass=com.observability.examples.AllExportersTest"]
