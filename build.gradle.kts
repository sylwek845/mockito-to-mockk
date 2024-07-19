import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
}

group = "com.sylwek845.mockito_mock"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.sarahbuisson:kotlin-parser:1.5")
    testImplementation(kotlin("test"))
//    testImplementation("junit:junit:4.13.2")
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