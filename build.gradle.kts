plugins {
    java
    id("io.github.sgtsilvio.gradle.defaults")
    id("com.github.hierynomus.license")
}

group = "com.hivemq.extensions.google.cloud.pubsub.customizations"
description = "Hello World Customization for the HiveMQ Enterprise Extensions for Google Cloud Pub/Sub"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.hivemq:hivemq-google-cloud-pubsub-extension-customization-sdk:${property("hivemq-google-cloud-pubsub-sdk.version")}")
    implementation("com.hivemq:hivemq-extension-sdk:${version}")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit-jupiter.version")}")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito:mockito-core:${property("mockito.version")}")
    testRuntimeOnly("org.slf4j:slf4j-simple:${property("slf4j-simple.version")}")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<Jar>().configureEach {
    manifest.attributes(
            "Implementation-Title" to project.name,
            "Implementation-Vendor" to "HiveMQ GmbH",
            "Implementation-Version" to project.version)
}

license {
    header = rootDir.resolve("HEADER")
    mapping("java", "SLASHSTAR_STYLE")
}