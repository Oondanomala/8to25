# 8to25 - Running Forge `1.8.9` on Java 25!

8to25 is a **client side only** Forge mod for `1.8.9` that allows the game to launch on Java 25,
unlocking higher performance and lower memory usage.
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

---

If you are not using a MultiMC based launcher, please switch to a MultiMC based launcher.
You'll have a better time anyway. The mod _will_ work on any launcher,
but you will have to figure out how to install it yourself :)

## Compatibility

Most mods should work just fine.
If they do not, please make an issue and I'll try to fix it!

> [!WARNING]
> If any of the mods require Mixin then [MixinBooter](https://github.com/CleanroomMC/MixinBooter)
> must be installed for things to work!

### Known issues

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
