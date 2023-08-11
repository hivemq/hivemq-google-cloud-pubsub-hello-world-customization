plugins {
    java
    alias(libs.plugins.defaults)
    alias(libs.plugins.license)
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
    implementation(libs.hivemq.googleCloudPubSubExtension.customizationSdk)
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito)
    testRuntimeOnly(libs.slf4j.simple)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<Jar>().configureEach {
    manifest.attributes(
        "Implementation-Title" to project.name,
        "Implementation-Vendor" to "HiveMQ GmbH",
        "Implementation-Version" to project.version,
    )
}

license {
    header = rootDir.resolve("HEADER")
    mapping("java", "SLASHSTAR_STYLE")
}
