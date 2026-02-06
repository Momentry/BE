FROM gradle:8.7.0-jdk21 AS build
WORKDIR /app

# Cache dependencies first
COPY gradle/ gradle/
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY settings.gradle build.gradle ./
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew --no-daemon dependencies

# Build application
COPY src/ src/
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew --no-daemon clean bootJar

# Extract layered jar
RUN java -Djarmode=layertools -jar build/libs/*.jar extract

FROM gcr.io/distroless/java21-debian12
WORKDIR /app

COPY --from=build /app/dependencies/ ./dependencies/
COPY --from=build /app/spring-boot-loader/ ./spring-boot-loader/
COPY --from=build /app/snapshot-dependencies/ ./snapshot-dependencies/
COPY --from=build /app/application/ ./application/

EXPOSE 8080

ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","org.springframework.boot.loader.launch.JarLauncher"]
