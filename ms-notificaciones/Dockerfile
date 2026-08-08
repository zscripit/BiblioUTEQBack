# --- Etapa 1: build ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline || true
COPY . .
RUN chmod +x mvnw && ./mvnw -q -B clean package -DskipTests

# --- Etapa 2: runtime ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
