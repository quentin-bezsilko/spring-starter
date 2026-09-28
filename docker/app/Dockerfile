FROM eclipse-temurin:25-jre

WORKDIR /app

COPY target/springstarter-0.0.1-SNAPSHOT.jar app.jar
COPY docker/otel/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/otel/opentelemetry-javaagent.jar", "-jar", "app.jar"]
