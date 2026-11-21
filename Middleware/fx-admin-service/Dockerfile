# Self-contained runtime image for fx-admin-service (Java 21 + Generational ZGC)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ARG JAR_FILE=target/fx-admin-service-*.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational -XX:MaxRAMPercentage=75.0"
ENV SERVER_PORT=8084

EXPOSE 8084

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
