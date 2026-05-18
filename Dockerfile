FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./gradlew fatJar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/YouTubeSpy.jar .
ENTRYPOINT ["java", "-jar", "YouTubeSpy.jar"]