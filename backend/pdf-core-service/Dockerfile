FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY pdf-core-service/target/pdf-core-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
