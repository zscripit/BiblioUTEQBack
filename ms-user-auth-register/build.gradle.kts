plugins {
    java
    id("org.springframework.boot") version "3.3.5" // Rama ultra-estable de producción
    id("io.spring.dependency-management") version "1.1.6"
}

// ... tus bloques de group, version y java toolchain se quedan igual (Java 21)

repositories {
    mavenCentral()
}

// Tren de Spring Cloud compatible al 100% con Boot 3.3.x
extra["springCloudVersion"] = "2023.0.3"

dependencies {
    // Tus starters base de negocio
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // El motor de OAuth 2.1 (Funciona impecable en la 3.3.x)
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")

    // El cliente para conectarse a Eureka
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // Drivers y herramientas
    runtimeOnly("org.postgresql:postgresql")
    compileOnly("org.projectlombok:lombok:1.18.40")
    annotationProcessor("org.projectlombok:lombok:1.18.40")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}