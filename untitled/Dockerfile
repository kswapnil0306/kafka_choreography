# Build with official Gradle image (works even if local gradlew/wrapper is broken or missing)
FROM gradle:8.6-jdk11 AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
RUN gradle bootJar --no-daemon --no-watch-fs

FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
