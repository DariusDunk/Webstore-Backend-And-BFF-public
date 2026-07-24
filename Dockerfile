FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 1620

ENTRYPOINT ["java","-jar","app.jar"]