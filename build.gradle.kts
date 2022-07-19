plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version ("6.1.0")
}

group = "me.waliedyassen.rscp"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("com.github.ajalt.clikt:clikt:3.5.0")
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger:1.0.4")
    implementation("ch.qos.logback:logback-core:1.2.11")
    implementation("ch.qos.logback:logback-classic:1.2.11")
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "me.waliedyassen.rscp.MainKt"
        }
    }
    shadowJar {
        archiveClassifier.set("")
        archiveVersion.set("")
        archiveBaseName.set("packer")
    }
}