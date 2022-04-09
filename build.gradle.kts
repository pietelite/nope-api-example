import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.0.0"
}

group = "me.pietelite"
version = "0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.pietelite.nope:common-api:BETA-1.1")
    implementation("me.pietelite.nope:sponge-api:BETA-1.1")
}

sponge {
    apiVersion("8.0.0")
    license("All Rights Reserved")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("nope-addon") {
        displayName("Nope Addon")
        entrypoint("me.pietelite.nopeapiexamplesponge.NopeApiExampleSponge")
        description("An example addon to Nope")
        links {
            homepage("https://github.com/pietelite/nope-api-example-sponge")
        }
        contributor("PietElite") {
            description("Author")
        }
        dependency("nope") {
            version("BETA-1.1.0")
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val javaTarget = 8 // Sponge targets a minimum of Java 8
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
    if (JavaVersion.current() < JavaVersion.toVersion(javaTarget)) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
