plugins {
    idea
    java
    id("gg.essential.loom") version "1.15.+"
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
            property("file.encoding", "UTF-8")
            property("java.system.class.loader", "com.gtnewhorizons.retrofuturabootstrap.RfbSystemClassLoader")
            vmArgs("--enable-native-access", "ALL-UNNAMED")
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
    maven("https://nexus.gtnewhorizons.com/repository/public/")
}

configurations.configureEach {
    // Replaced by RFB
    exclude("net.minecraft", "launchwrapper")
    // Exclude ASM 5
    exclude("org.ow2.asm", "asm-debug-all")
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    // Explicitly depend on RFB so other mods can easily depend on this mod
    // Transitively depends on modern ASM (required for modern Java compatibility)
    api(libs.rfb)
    // Updated game dependencies, should be backwards compatible
    api("org.apache.commons:commons-lang3:3.18.0")
        ?.because("Makes the SystemUtil class work in newer Java")
    api("org.apache.commons:commons-compress:1.28.0")
        ?.because("Non JDK Pack200 implementation")
    // "100% binary compatible, 1 minor source compatibility issue (removed a throws IOException clause)" - lwjgl3ify
    api("com.google.code.gson:gson:2.13.2") {
        because("Not necessary, but nice to update")
        exclude("com.google.errorprone") // Unnecessary annotations
    }

    // Cannot be shaded because Forge will not be able to
    // recognize the mod jar when RFB is not present otherwise
    implementation(libs.reflect)
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

    jar {
        manifest.attributes(mapOf(
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
        ))
    }
}
