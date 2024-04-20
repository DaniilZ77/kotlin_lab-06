group = "csc.makrobot"
version = "1.0-SNAPSHOT"

plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.commons.compress)
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.ktcompile.testing)
}

tasks.test {
    // dependsOn(tasks.withType<KtLintCheckTask>())
    // Enable JUnit 5 (Gradle 4.6+).
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(JavaVersion.VERSION_21.majorVersion.toInt())
}
