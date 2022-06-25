# Contributing
## In short
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
1. Import the project into your preferred IDE.
    1. If you use IntelliJ (the preferred option), you can simply import the project as a Gradle project.
    1. If you use Eclipse, you need to `./gradlew eclipse` before importing the project as an Eclipse project.
1. Edit the code
1. After testing in the IDE, build a JAR to test whether it works outside the IDE too
   ```
   ./gradlew build
   ```
    - Note: sometimes, especially on development versions, the tests may fail. To skip the tests, use `./gradlew build -x test`

   The mod JAR may be found in the `build/libs` directory
1. [Create a pull request](https://help.github.com/en/articles/creating-a-pull-request)
   so that your changes can be integrated into clientcommands
    - Note: for large contributions, create an issue before doing all that
      work, to ask whether your pull request is likely to be accepted

## Code style
Please adhere to these styling instructions when contributing. If you don't do this initially you will be requested to change your commits accordingly.
These instructions are not foundational. You're expected to have an understanding of what is clean Java code and what isn't. These instructions build on general consensus.

You may see old code that doesn't adhere to these guidelines, but all new code must do. If you are working nearby old code, you can update it to fit the guidelines if you want to.
### Variables
#### Naming
* Use descriptive variable names everywhere
* Loop indices can be an exception to the above instruction
   * Never use `j`. If you find yourself wanting to use `j`, it's a sign that you should rename your loop variables to something more descriptive. You can use `i` but no more than that
   * Loops with small bodies (not more than a few lines) may use `i` as the loop variable, unless there is something obvious that's more descriptive
   * Loops with large bodies may only use `i` if the loop variable is unused within the loop body
#### Immutable constants
* Immutable constants should use UPPER_SNAKE_CASE
* Mark immutable constants as `private static final`
### Statements
#### `if`, `for` and `while` statements
* `if`, `for` and `while` statements should always use braces
* There should be a space between the keyword and the statement-bracket
#### `import` statements
* `import static`s should always use wildcard imports
   * They should be placed after the other imports
   * Don't use `import static` for anything else than commands
* Use import statements rather than fully qualified class names
### API and Documentation
* Public API is found in the API source set.
  * All public classes and methods in the API should have a Javadoc comment.
  * Do not make breaking changes in the API, i.e. changes that would cause mods compiled against the API to break.
  * One way to avoid API breakages is to make a list of parameters into an object. Then you can add fields and methods to that object without changing the number of parameters to the method.
  * All the rest of the code is implementation details, where you may freely break backwards compatibility.
* Aim for code to be self-documenting. Comments should make code quicker to read, not add noise.
* Large amounts of documentation for a subsystem can go in markdown files in the `docs` folder.
### General
* Any message the player may receive should be translated
    * Except for debugging (log messages, debug HUD)
* Don't use Java features only available in Java versions newer than the one Minecraft uses, if applicable
* All files should have a newline at the end of the file
* Do not use AWT at all

## Project Structure
Multiconnect is split into several functionalities. The main one is translating packets as they go back and forth between the client and the server, but there are also other functionalities such as modifying client movement and miscellaneous stuff like the version dropdown code.

* `annotationProcessor` - the annotation processor for the translator compiler. Scrapes the necessary data from the Java code and puts it into easy-to-parse temporary json files for the actual compiler.
* `annotations` - the annotations necessary for the translator compiler, used by the main multiconnect project and the translator compiler subprojects. It's in its own subproject to avoid circular dependencies.
* `data` - data files for the translator compiler (mostly registries, also a list of protocols).
* `src/api` - the multiconnect API
* `src/generated` - output code from the translator compiler, not to be edited manually.
* `src/main` - the main multiconnect code
* `src/tools` - miscellaneous tools for development
* `translatorCompiler` - the translator compiler implementation

### Translator Compiler
Translation, serialization and deserialization of packets is not usually written by hand. Instead, packets are written declaratively in the `net.earthcomputer.multiconnect.packets` package, and registry IDs that need converting between are written declaratively in the `data` directory. The easiest way to get a feel for how to write these packets is to look at the classes in this package.

A detailed specification of the translator compiler can be found in [translator_compiler.md](translator_compiler.md).

### Client-side fixes
Client-side-only fixes (e.g. to player movement) are implemented mostly in mixins. Any version-specific client-side fix code should go in the `net.earthcomputer.multiconnect.protocols.<version>` package.

### Where to put changes
You should always prefer to put changes at the network level where possible. This ensures greater maintainability and compatibility with other mods. Usually, this means using the translator compiler. In cases where it's difficult to put on the network layer but theoretically possible, it may be acceptable to modify the game more instead, but this should be a last resort to getting something released, and should be moved to the network layer later on. Some things are impossible to implement on the network layer, such as changes to player movement.

### Threading Model
Unlike most mods, you have two threads to think about in multiconnect, and you must be mindful of possible data races between the two.
1. Game thread - where most of the game processing happens, most packets are handled by the game on this thread
2. Netty event loop (commonly referred to in multiconnect as the network thread) - this is the thread where packets are serialized and deserialized by Minecraft, and the thread in which translation occurs in multiconnect. All packet translation code is called on this thread.
3. In the future multiconnect may add extra worker threads to speed up translation.

One particularly annoying consequence of this threading model is that you cannot access the parts of the game you normally would to get some global variable, for example the dimension you are currently in. If you were to do this, you may for instance try to deserialize a chunk data packet for the nether before the packet telling the client you are in the nether has been handled. The solution is to use `@GlobalData` to share data between packets on the network thread. For example, the two packets that set your dimension, `SPacketGameJoin` and `SPacketPlayerRespawn`, store the dimension in global data in their `@PartialHandler` methods, and then any packets that need that data later on may retrieve it from global data.

### Debugging
As you're contributing to multiconnect, it may be helpful to debug it. The most helpful class to look at here is `DebugUtils`:
* `onDebugKey` - called when the multiconnect debug key (F8) is pressed while in-game, if the system property for it is enabled. This allows you to trigger an action on demand in-game. Change the content of this method to what you need.
* `handlePacketDump` - handles an encoded packet dump that appears in a game log. This forces the game to receive the exact same packet data as the user who uploaded the game log did, which makes it easy to reproduce the issue.

To replay a packet log, make sure there is a file named `replay.log` (for uncompressed logs) or `replay.log.gz` (for compressed logs) in `.minecraft/config/multiconnect/packet-logs`, then press the multiconnect debug key (F8) in the title screen after enabling the system property for it (see below).

Debug system properties:
* `multiconnect.debugKey` (boolean) - enables the multiconnect debug key (F8).
* `multiconnect.enablePacketRecorder` (boolean) - enables the packet recorder, which can then be replayed later.
* `multiconnect.unitTestMode` (boolean) - enables unit test mode.
* `multiconnect.ignoreErrors` (boolean) - drops packets that have translation errors rather than disconnecting the client. This may be useful to log in to a server to get into a state where you can press the debug key.
* `multiconnect.dumpRegistries` (boolean) - dumps the registries for the current version in the `data` directory on game startup. Useful for updating to a new version.
* `multiconnect.skipTranslation` (boolean) - skips multiconnect translation, useful for debugging how vanilla handles a packet.

## Miscellaneous
* Please do not copy code whose license doesn't allow to copy without also copying license headers. E.g. ViaVersion.
