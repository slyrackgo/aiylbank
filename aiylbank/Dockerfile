FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/aiyl-bank-1.0.0.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker \
    DB_HOST=db \
    DB_PORT=5432 \
    DB_NAME=aiylbank \
    DB_USERNAME=postgres \
    DB_PASSWORD=1234

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]

