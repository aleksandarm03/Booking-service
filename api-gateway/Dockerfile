FROM eclipse-temurin:17-jre
ARG JAR=target/api-gateway-0.0.1-SNAPSHOT.jar
WORKDIR /app
COPY ${JAR} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

