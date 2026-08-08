# --- Etapa 1: build ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true
COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar -x test

# --- Etapa 2: runtime ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /tmp/libs/
RUN JAR=$(ls /tmp/libs/*.jar | grep -v plain) && cp "$JAR" /app/app.jar && rm -rf /tmp/libs

EXPOSE 8088
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
