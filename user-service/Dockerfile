FROM eclipse-temurin:17-jre
ARG JAR=target/user-service-0.0.1-SNAPSHOT.jar
WORKDIR /app
COPY ${JAR} app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]

