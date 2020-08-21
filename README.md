![Project icon](https://raw.githubusercontent.com/Earthcomputer/multiconnect/master/src/main/resources/assets/multiconnect/icon.png)

# multiconnect
![GitHub license](https://img.shields.io/github/license/Earthcomputer/multiconnect.svg)
![GitHub issues](https://img.shields.io/github/issues/Earthcomputer/multiconnect.svg)
![GitHub tag](https://img.shields.io/github/tag/Earthcomputer/multiconnect.svg)

A mod to enable a Minecraft client to connect to multiple server versions.

## Installation for Players
1. Download and run the [Fabric installer](https://fabricmc.net/use).
   - Click the "vanilla" button, leave the other settings as they are,
     and click "download installer".
   - Note: this step may vary if you aren't using the vanilla launcher
     or an old version of Minecraft.
1. Download multiconnect from the [releases page](https://github.com/Earthcomputer/multiconnect/releases)
   and move it to the mods folder (`.minecraft/mods`).

## Installation for Mod Developers
This section is for when you are developing your own mod and want to use the multiconnect API, or run multiconnect alongside your mod in the IDE. Aside from the first step, you ONLY need to follow the steps applicable to you and your mod.
1. Add a `repositores {}` block in your `build.gradle` (if there isn't already one), and add Earthcomputer's Bintray mods repository:
   ```groovy
   repositories {
      maven {
         url 'https://dl.bintray.com/earthcomputer/mods'
      }
   }
   ```
   - Note: this repositories block is NOT the same as the one inside the `buildscript {}` block.
1. If you want to use the API inside your mod, you will have to jar-in-jar it for the release and add it to the classpath. To do this, add the following to your `dependencies {}` block:
   ```groovy
   dependencies {
      // ...
      modImplementation 'net.earthcomputer:multiconnect:<version>:api'
      include 'net.earthcomputer:multiconnect:<version>:api'
   }
   ```
   - Note: replace `<version>` with the version of multiconnect you want to depend on.
   - Note: SKIP the `include` part if your mod is NOT using the API in any way.
1. If you want to run multiconnect in the IDE alongside your mod, add the following to your `dependencies {}` block:
   ```groovy
   dependencies {
      // ...
      modRuntime 'net.earthcomputer:multiconnect:<version>:slim'
   }
   ```
   - Note: the previous step should also be done alongside this step.
   - Note: this step is only necessary if you want to run the full mod in the IDE. Otherwise you can skip this step.

## Contributing
1. Clone the repository
   ```
   git clone https://github.com/Earthcomputer/multiconnect
   cd multiconnect
   ```
1. Generate the Minecraft source code
   ```
   ./gradlew genSources
   ```
   - Note: on Windows, use `gradlew` rather than `./gradlew`.
1. Generate the IDE project depending on which IDE you prefer
   ```
   ./gradlew idea      # For IntelliJ IDEA
   ./gradlew eclipse   # For Eclipse
   ```
1. Import the project in your IDE and edit the code
1. After testing in the IDE, build a JAR to test whether it works outside the IDE too
   ```
   ./gradlew build
   ```
   The mod JAR may be found in the `build/libs` directory
1. [Create a pull request](https://help.github.com/en/articles/creating-a-pull-request)
   so that your changes can be integrated into multiconnect
   - Note: for large contributions, create an issue before doing all that
     work, to ask whether your pull request is likely to be accepted
