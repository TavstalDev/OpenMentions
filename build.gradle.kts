plugins {
    // Apply the Java plugin for building Java projects
    id("java")
    // Apply the Shadow plugin for creating fat JARs
    id("com.gradleup.shadow") version "8.3.0"
    // Apply the Run-Paper plugin for running Paper Minecraft servers
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

// Define project properties for versions and package name
val javaVersion: String by project
val paperApiVersion: String by project
val snakeYamlVersion: String by project
val hikariCpVersion: String by project
val mineCoreLibVersion: String by project
val xseriesVersion: String by project
val sirblobmanApiVersion: String by project
val sirblobmanCombatLogVersion: String by project
val caffeineVersion: String by project
val projectPackageName = "${project.group}.openMentions"

// Configure Java toolchain and compatibility settings
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
}

// Define repositories for dependency resolution
repositories {
    mavenCentral() // Central Maven repository
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://jitpack.io") // JitPack repository
    }
    maven {
        name = "sirblobman-public" // CombatLogX
        url = uri("https://nexus.sirblobman.xyz/public/")
    }
}

dependencies {
    // Paper API for Minecraft server development
    compileOnly("io.papermc.paper:paper-api:${paperApiVersion}")

    compileOnly("com.github.sirblobman.api:core:${sirblobmanApiVersion}")
    compileOnly("com.github.sirblobman.combatlogx:api:${sirblobmanCombatLogVersion}")

    // HikariCP for database connection pooling
    implementation("com.zaxxer:HikariCP:${hikariCpVersion}")
    // Custom library for core functionality
    implementation(files("libs/MineCoreLib-${mineCoreLibVersion}.jar"))
    // XSeries
    implementation("com.github.cryptomorin:XSeries:${xseriesVersion}")
    // SQL caching
    implementation("com.github.ben-manes.caffeine:caffeine:${caffeineVersion}")
}

// Disable the default JAR task
tasks.jar {
    enabled = false
}

// Configure the Shadow JAR task
tasks.shadowJar {
    archiveClassifier.set("") // Set the classifier for the JAR
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot" // Add custom manifest attributes
    }

    exclude("com/google/**")
    exclude("org/jspecify/**")
    exclude("org/slf4j/**")

    // Relocate packages to avoid conflicts
    relocate("com.zaxxer.hikari", "${projectPackageName}.shadow.hikari")
    relocate("com.cryptomorin.xseries", "${projectPackageName}.shadow.xseries")
    relocate("com.github.benmanes.caffeine", "${projectPackageName}.shadow.caffeine")
}

// Ensure the Shadow JAR task runs during the build process
tasks.build {
    dependsOn(tasks.shadowJar)
}

// Configure additional tasks
tasks {
    // Configure the RunServer task for running a Paper server
    named<xyz.jpenilla.runpaper.task.RunServer>("runServer") {
        minecraftVersion("1.21") // Specify the Minecraft version
    }

    // Configure Java compilation settings
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8" // Set the file encoding
        val javaVersionInt = javaVersion.toInt()
        if (javaVersionInt >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(javaVersionInt) // Set the release version for Java
        }
    }

    // Process resources and expand placeholders in `plugin.yml`
    processResources {
        val props = mapOf("version" to project.version.toString()) // Define properties for resource filtering
        inputs.properties(props)
        filteringCharset = "UTF-8" // Set the charset for filtering
        filesMatching("plugin.yml") {
            expand(props) // Replace placeholders in `plugin.yml`
        }
    }
}