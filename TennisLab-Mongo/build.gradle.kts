import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    application
    kotlin("plugin.serialization") version "1.7.20"

    // Para ktorfit que usa KSP
    // Plugin KSP para generar código en tiempo de compilación ktorfit
    id("com.google.devtools.ksp") version "1.7.21-1.0.8"
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

group = "es.ivanloli"
version = "1.0-SNAPSHOT"
val koin_version= "3.3.2"
val koin_ksp_version= "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    implementation("org.litote.kmongo:kmongo-async:4.7.2")
    // Usamos corrutinas para ello
    implementation("org.litote.kmongo:kmongo-coroutine:4.7.2")

    // Para cifrar las contraseñas
    implementation("com.ToxicBakery.library.bcrypt:bcrypt:+")

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // Para hacer el logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("ch.qos.logback:logback-classic:1.4.5")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Ktorfit, es decir Ktor client modificado para parecerse a Retrofit
    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:1.0.0-beta16")
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:1.0.0-beta16")
    // Para serializar en Json con Ktor
    implementation("io.ktor:ktor-client-serialization:2.1.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.1.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.3")

    // Koin Core features
    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.insert-koin:koin-annotations:$koin_ksp_version")
    ksp("io.insert-koin:koin-ksp-compiler:$koin_ksp_version")
    // Koin Test features
    testImplementation("io.insert-koin:koin-test:$koin_version")

    testImplementation("io.insert-koin:koin-test-junit5:$koin_version")
    // Para testear con corrutinas
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    // services.cache
    implementation("io.github.reactivecircus.cache4k:cache4k:0.9.0")

    implementation("com.auth0:java-jwt:4.2.1")

    //testImplementation(kotlin("test"))
    // MockK para testear Mockito con Kotlin
    testImplementation("io.mockk:mockk:1.13.4")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}