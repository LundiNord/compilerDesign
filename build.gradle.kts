plugins {
    id("java")
    application
    id("org.sonarqube") version "6.0.1.5171"
}

group = "edu.kit.kastel.logic"
version = "1.0-SNAPSHOT"

application {
    mainModule = "edu.kit.kastel.vads.compiler"
    mainClass = "edu.kit.kastel.vads.compiler.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jspecify:jspecify:1.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(24)
}

tasks.test {
    useJUnitPlatform()
}

sonar {
  properties {
    property("sonar.projectKey", "LundiNord_compilerDesign")
    property("sonar.organization", "lundinord")
    property("sonar.host.url", "https://sonarcloud.io")
  }
}