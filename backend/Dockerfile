FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/firebase-service-account.json

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "if [ -n \"$FIREBASE_SERVICE_ACCOUNT_BASE64\" ]; then echo $FIREBASE_SERVICE_ACCOUNT_BASE64 | base64 -d > /app/firebase-service-account.json; fi && java -jar app.jar"]