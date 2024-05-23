FROM gradle:jdk17 as build
WORKDIR /app
COPY . .
RUN ./gradlew build --no-daemon

FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]