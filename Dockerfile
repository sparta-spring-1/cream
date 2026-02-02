FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY gradle ./gradle

RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew clean build -x test --no-daemon

FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S creamgroup && adduser -S creamuser -G creamgroup
USER creamuser

COPY --from=builder /app/build/libs/*.jar app.jar

LABEL authors="Chae"
LABEL project="cream"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
