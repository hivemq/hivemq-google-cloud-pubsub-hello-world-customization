rootProject.name = "hivemq-google-cloud-pubsub-hello-world-customization"

pluginManagement {
    plugins {
        id("com.github.hierynomus.license") version "${extra["plugin.license.version"]}"
        id("com.github.sgtsilvio.gradle.utf8") version "${extra["plugin.utf8.version"]}"
    }
}

if (file("../hivemq-google-cloud-pubsub-extension-customization-sdk").exists()) {
    includeBuild("../hivemq-google-cloud-pubsub-extension-customization-sdk")
}