plugins {
    kotlin("jvm") version "1.9.25"         // Kotlin support
    id("org.jetbrains.intellij") version "1.17.2" // IntelliJ plugin build
}

group = "com.manishgarhwal.dcm_report"
version = "1.0.0"

repositories {
    mavenCentral()
}

intellij {
    // Use a version compatible with Android Studio Hedgehog (2023.x) or Iguana (2024.x)
    version.set("2024.1")
    type.set("IC")        // Base type: IntelliJ IDEA Community
    plugins.set(listOf())
}

tasks {
    patchPluginXml {
        sinceBuild.set("223")   // works from IntelliJ 2022.3
        untilBuild.set("243.*")
    }

    buildPlugin {
        archiveFileName.set("dcm-report-plugin-${version}.zip")
    }
}
