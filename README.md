![Project icon](https://raw.githubusercontent.com/Earthcomputer/multiconnect/master/src/main/resources/assets/multiconnect/icon.png)

# multiconnect
![GitHub license](https://img.shields.io/github/license/Earthcomputer/multiconnect.svg)
![GitHub issues](https://img.shields.io/github/issues/Earthcomputer/multiconnect.svg)
![GitHub tag](https://img.shields.io/github/tag/Earthcomputer/multiconnect.svg)

Connect to many different server versions from your Fabric client!

## Social
Discord: https://discord.gg/Jg7Bun7
Patreon: https://www.patreon.com/earthcomputer

## Why another protocol translator?
Multiconnect distinguishes itself from similar projects such as ViaVersion and ProtocolSupport in that it is very much client-side. Crucially, this means it can (and does) fix many version differences on the client; such issues are unfixable on the server. As of the time of writing, multiconnect is the only mod that fixes 1.12 swimming mechanics, parkour differences, and re-adds removed features such as command syntax on the client.

On the other hand, multiconnect supports only the latest Minecraft client version and only on Fabric. It also does not have as much version coverage as ViaVersion and ProtocolSupport (yet).

### Usage with ViaFabric
If you want to connect to servers older than what multiconnect can currently support, you can install [ViaFabric](https://github.com/ViaVersion/ViaFabric) alongside multiconnect. The two are compatible - multiconnect will translate as much as it can and ViaFabric will take you the rest of the way. Follow the instructions in the ViaFabric readme for details on how to do this.

### Other alternatives
- [ClientViaVersion](https://github.com/Gerrygames/ClientViaVersion): a discontinued plugin for The 5zig Mod
which supported client versions 1.7.10, 1.8, 1.8.9, 1.12 and 1.12.2.
- [ViaVersion](https://viaversion.com/), [ProtocolSupport](https://protocol.support/): server-side protocol
bridges.

## Installation for Players
1. Download and run the [Fabric installer](https://fabricmc.net/use).
   - Click the "vanilla" button, leave the other settings as they are,
     and click "download installer".
   - Note: this step may vary if you aren't using the vanilla launcher
     or an old version of Minecraft.
1. Download multiconnect from the [releases page](https://github.com/Earthcomputer/multiconnect/releases)
   and move it to the mods folder (`.minecraft/mods`).

## Build Instructions
1. Building requires JDK17.
2. 
   1. On Windows, run `gradlew build`
   2. On Linux and MacOS, run `./gradlew build`
   3. Note: sometimes, especially on development versions, the tests may fail. To skip the tests, use `./gradlew build -x test`
3. The JAR file can be found in `build/libs` (it's the one with the shortest name).

## Installation for Mod Developers
Looking to support custom payloads? Check out [this document](docs/custom_payloads.md).

This section is for when you are developing your own mod and want to use the multiconnect API, or run multiconnect alongside your mod in the IDE. Aside from the first step, you ONLY need to follow the steps applicable to you and your mod.
1. Explicitly setting a repository is not necessary, as multiconnect is hosted on Maven Central.
1. If you want to use the API inside your mod, you will have to jar-in-jar it for the release and add it to the classpath. To do this, add the following to your `dependencies {}` block:
   ```groovy
   dependencies {
      // ...
      modImplementation('net.earthcomputer.multiconnect:multiconnect-api:<version>') { transitive = false }
      include('net.earthcomputer.multiconnect:multiconnect-api:<version>') { transitive = false }
   }
   ```
   - Note: replace `<version>` with the version of multiconnect you want to depend on.
   - Note: SKIP the `include` part if your mod is NOT using the API in any way.
1. If you want to run multiconnect in the IDE alongside your mod, add the following to your `dependencies {}` block:
   ```groovy
   dependencies {
      // ...
      modRuntimeOnly('net.earthcomputer.multiconnect:multiconnect-slim:<version>') { transitive = false }
   }
   ```
   - Note: the previous step should also be done alongside this step.
   - Note: this step is only necessary if you want to run the full mod in the IDE. Otherwise you can skip this step.

## Contributing
See [contributing.md](docs/contributing.md)
