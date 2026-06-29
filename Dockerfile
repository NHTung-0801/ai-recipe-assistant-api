FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy file jar được build từ target vào container
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose port 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]