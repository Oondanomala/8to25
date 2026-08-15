# 8to25 - Running Forge `1.8.9` on Java 25+!

8to25 is a **client side only** Forge mod for `1.8.9` that allows the game to launch on Java 25+,
unlocking higher performance and lower memory usage. As of `1.2.0`, it also bundles an
updated and backwards-compatible version of [Mixin](https://github.com/Oondanomala/Mixin8)
and [MixinExtras](https://github.com/LlamaLad7/MixinExtras), which brings modern Mixin features,
better performance, and a much improved developer experience.
Unlike lwjgl3ify, this mod does not make the game run under LWJGL3, LWJGL2 is still used.

> [!NOTE]
> This is a client side only mod, and only client side mods are supported.
> Mods that add new content are unlikely to work
> (though there isn't a technical reason why they can't, so you're welcome to try).

Most of the code is based off of [lwjgl3ify](https://github.com/GTNewHorizons/lwjgl3ify),
credit goes to its developers :)

## Installation

Unlike normal mods, extra steps are required to get things working.

### MultiMC based launchers (like Prism)

In the instance options "Version" menu, find the `Minecraft` and `Forge` entries,
click "Customize" on the right (or in the right-click menu), then "Edit".
Replace the JSONs with [net.minecraft.json](launcher-json/net.minecraft.json) and [net.minecraftforge.json](launcher-json/net.minecraftforge.json) respectively.

Then, add the following JVM arguments (instance options -> Settings -> Java -> Java Arguments)
```
-Dfile.encoding=UTF-8 -Djava.system.class.loader=com.gtnewhorizons.retrofuturabootstrap.RfbSystemClassLoader --enable-native-access ALL-UNNAMED
```
and choose a modern JDK to run the game (Java 25 is recommended).
You might also want to consider adding `-XX:+UseCompactObjectHeaders` to the JVM arguments
if using Java 25+, for lower memory usage.

> [!IMPORTANT]
> If you are using Linux you'll need to set `useNativeTransport` to false
> in `options.txt` to be able to join servers!

---

If you are not using a MultiMC based launcher, please switch to a MultiMC based launcher.
You'll have a better time anyway. The mod _will_ work on any launcher,
but you will have to figure out how to install it yourself :)

## Compatibility

Most mods should work just fine.
If they do not, please make an issue and I'll try to fix it!

### Known issues

- As of `1.2.0` Mixin is bundled with the mod, so [MixinBooter](https://github.com/CleanroomMC/MixinBooter)
  is no longer necessary and is fully incompatible.
  If a mod explicitly depends on MixinBooter, please report it as a bug.
- Some mods may not work because of this [RetroFuturaBootstrap bug](https://github.com/GTNewHorizons/RetroFuturaBootstrap/pull/17),
  please report broken mods as it can usually be worked around.
- Optifine's native memory usage tracker shown in the F3 debug menu does not work and will always show `0`.
  This is harmless and can be ignored.

## How does it work?

It's actually surprisingly simple!

- LaunchWrapper is replaced with RetroFuturaBootstrap, an open source replacement that supports modern Java,
  makes existing code compatible with newer ASM, and includes an improved class transformer API.
- The [Reflect](https://github.com/Lenni0451/Reflect) library is used to open all modules and enable unnamed native access.
- Forge's `EnumHelper` and `ObjectHolderRef` classes are transformed to replace usage of internal reflection API
  with Reflect's Java 8-26 compatible API.
- Forge's `ASMModParser` class is transformed to fix a bug that would cause log spam.
- Forge's `TerminalTransformer` is skipped because the `SecurityManager` is gone in Java 17+ and 
  all this does is add unnecessary log messages.
- Forge's `ClassPatchManager` is transformed to fix a couple bugs that only show up when using this mod.
- Apache Commons Lang is updated to fix a compatibility issue with modern Java,
  and Apache Commons Compress and its dependencies are updated for the following bullet point.
- Usage of the now removed `Pack200` class is redirected to the Apache Commons Compress implementation
  (with a compatibility shim to work around Forge bugs).
- The modern Mixin loader is based off of the [Fabric fork](https://github.com/FabricMC/Mixin),
  which already has almost complete backwards compatibility with Mixin `0.7.11`. With a
  [few simple tweaks](https://github.com/FabricMC/Mixin/compare/main...Oondanomala:Mixin8:main), it's ready to go!

## Using modern Java in your own mod

If you want to use modern Java, modern Mixin/MixinExtras, or any of the other updated dependencies this mod provides
in your own mod you can simply depend on this mod like this:

In `build.gradle.kts`:
```kotlin
loom {
    runConfigs {
        getByName("client") {
            // To make dev env work with RFB
            property("file.encoding", "UTF-8")
            property("java.system.class.loader", "com.gtnewhorizons.retrofuturabootstrap.RfbSystemClassLoader")
            mainClass.set("com.gtnewhorizons.retrofuturabootstrap.Main")
            // To initialize Mixin in your dev env, remove if you don't use it
            programArgs("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
        }
    }
}

repositories {
    // Required for Mixin
    maven("https://jitpack.io") {
        content { includeGroup("com.github.Oondanomala") }
    }
    // Required for RFB
    maven("https://nexus.gtnewhorizons.com/repository/public/")
}

configurations.configureEach {
    // Replaced by 8to25
    exclude("net.minecraft", "launchwrapper")
    exclude("org.ow2.asm", "asm-debug-all")
}

dependencies {
    // modImplementation is not needed as it does not touch obfuscated code
    implementation("me.oondanomala.eightto25.8to25:LATEST-TAG") {
        // If your mod does not use Mixin, you can exclude it from the dependency
        //exclude(module = "Mixin8")
        //exclude(module = "mixinextras-common")
    }
}
```

Then add the 8to25 source in `settings.gradle.kts`:
```kotlin
sourceControl {
    gitRepository(java.net.URI("https://github.com/Oondanomala/8to25.git")) {
        producesModule("me.oondanomala.eightto25:8to25")
    }
}
```

It's recommended you follow this [Fabric wiki page](https://docs.fabricmc.net/develop/getting-started/intellij-idea/launching-the-game#hotswapping-classes)
to get improved hotswap.
