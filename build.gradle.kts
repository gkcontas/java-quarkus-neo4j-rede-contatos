plugins {
    java
    id("io.quarkus") version "3.39.2"
}

group = "com.gkcontas"
version = "0.1.0-SNAPSHOT"
description = "Contact Network - graph modeling and queries with Quarkus and Neo4j"

repositories {
    mavenCentral()
}

val quarkusPlatformGroupId = "io.quarkus.platform"
val quarkusPlatformArtifactId = "quarkus-bom"
val quarkusPlatformVersion = "3.39.2"

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))

    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkiverse.neo4j:quarkus-neo4j:6.7.2")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
