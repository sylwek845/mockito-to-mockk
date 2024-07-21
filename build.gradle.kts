import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.sylwek845.mockito_mock"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    val junit5 = "5.9.0"
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junit5}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${junit5}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${junit5}")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}