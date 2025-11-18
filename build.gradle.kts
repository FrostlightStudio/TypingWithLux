plugins {
    kotlin("jvm") version "2.2.20-RC2"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.typewritermc.module-plugin") version "2.1.0"
}

group = "com.thedevjade"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven {
        name = "aselstudiosRepository"
        url = uri("https://repo.aselstudios.com/releases")
    }
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

typewriter {
    namespace = "thedevjade"

    extension {
        name = "TypingWithLux"
        shortDescription = "LuxDialogues in typewriter"
        description = "TypingWithLux is a simplistic extension that allows to interact with lux's api for dialogues and cinematics"
        engineVersion = "0.9.0-beta-167"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA

        paper {
            // Optional - If you want to make sure a plugin is required to be installed to use this extension
            dependency("LuxDialogues")
        }
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.aselstudios:LuxDialoguesAPI:3.0.4")
    implementation("com.typewritermc:RoadNetworkExtension:0.9.0")
    implementation("com.typewritermc:EntityExtension:0.9.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
