plugins {
    idea
    java
    id("gg.essential.loom") version "1.15.+"
    id("com.gradleup.shadow") version "9.4.+"
}

val modGroup: String by project
val modID: String by project
val modName: String by project
val modVersion: String by project

group = modGroup
version = modVersion

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    runConfigs {
        getByName("client") {
            property("java.system.class.loader", "com.gtnewhorizons.retrofuturabootstrap.RfbSystemClassLoader")
            mainClass.set("com.gtnewhorizons.retrofuturabootstrap.Main")
        }
        remove(getByName("server"))
    }

    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }

    // For some reason loom defaults to tab indentation
    decompilers {
        named("vineflower") {
            options.put("indent-string", "    ")
        }
    }
}

repositories {
    mavenCentral()
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://nexus.gtnewhorizons.com/repository/public/")
}

val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("net.minecraft:launchwrapper"))
            .using(module("com.gtnewhorizons.retrofuturabootstrap:RetroFuturaBootstrap:1.0.16"))
            .because("LaunchWrapper replacement")
    }
    exclude("org.ow2.asm", "asm-debug-all")
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    // Cannot be shaded as mod classes aren't loaded on launch
    val asmVersion = "9.9.1"
    implementation("org.ow2.asm:asm:$asmVersion")
    implementation("org.ow2.asm:asm-commons:$asmVersion")
    implementation("org.ow2.asm:asm-tree:$asmVersion")
    implementation("org.ow2.asm:asm-analysis:$asmVersion")
    implementation("org.ow2.asm:asm-util:$asmVersion")

    // Mod dependencies
    shade("net.lenni0451:Reflect:1.6.3")

    // Updated game dependencies
    // Required for:
    // - Making SystemUtils work in newer Java versions
    // - Pack200
    // Should be backwards compatible
    // Cannot shade, the user needs to modify minecraft.json directly
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("org.apache.commons:commons-compress:1.28.0")
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    processResources {
        inputs.property("modID", modID)
        inputs.property("modName", modName)
        inputs.property("version", version)

        filesMatching(listOf("mcmod.info", "META-INF/rfb-plugin/*")) {
            expand(inputs.properties) {
                escapeBackslash = true
            }
        }
    }

    shadowJar {
        archiveClassifier.set("dev")
        configurations = listOf(shade)
    }

    jar {
        manifest.attributes(mapOf(
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
        ))
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        archiveClassifier.set("")
    }
}
