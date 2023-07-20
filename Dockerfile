FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
ARG JAR_FILE
COPY target/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]