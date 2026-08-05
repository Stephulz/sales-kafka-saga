FROM eclipse-temurin:25-jre

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8093

ENTRYPOINT ["java", "-jar", "app.jar"]