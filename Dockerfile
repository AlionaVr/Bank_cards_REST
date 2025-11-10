FROM maven:3.9.1-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .

RUN mvn -q -e -B dependency:go-offline

COPY src ./src

RUN mvn -B -f pom.xml package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

