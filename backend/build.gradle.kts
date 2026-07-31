import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    id("org.springframework.boot") version "4.1.0"
}

group = "com.devhub"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Gradle 네이티브 BOM 방식
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // RSS/Atom 파싱
    implementation("com.rometools:rome:2.1.0")
    implementation("org.jsoup:jsoup:1.22.2")

    compileOnly(platform(SpringBootPlugin.BOM_COORDINATES))
    compileOnly("org.projectlombok:lombok")
    annotationProcessor(platform(SpringBootPlugin.BOM_COORDINATES))
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

fun registerTestTask(name: String, pattern: String, summary: String) =
    tasks.register<Test>(name) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = summary
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        filter { includeTestsMatching(pattern) }
    }

registerTestTask("unitTest", "*UnitTest*", "모든 단위 테스트 실행한다.")
registerTestTask("integrationTest", "*IntegrationTest*", "모든 통합 테스트를 실행한다.")