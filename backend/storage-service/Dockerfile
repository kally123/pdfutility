# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY ../pom.xml ../pom.xml
COPY ../common/ ../common/

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN ./mvnw package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create upload directories
RUN mkdir -p /app/uploads /app/temp && \
    chown -R appuser:appgroup /app

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app
USER appuser

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
