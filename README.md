# godot-java-demo-projects

Godot official demo projects rewritten in pure Java using [godot-java](https://github.com/youngledo/godot-java).

This project ports 107 demos from [godot-demo-projects](https://github.com/godotengine/godot-demo-projects) (originally written in GDScript) to pure Java, demonstrating that Java is a first-class language for Godot game development via GDExtension + Panama FFI.

## Quick Start

1. Clone this repository
2. Open any demo's directory in Godot 4.6+ Editor (e.g. `2d/pong/`)
3. Press F5 to run

Pre-built JARs and the native library are included in the `native/` directories — no build step required.

## Building from Source

Requirements: JDK 25+, Maven 4.0+

```bash
mvn package -DskipTests
```

This produces a fat JAR in each module's `native/` directory.

## Architecture

The project is organized as a Maven multi-module build. Each module contains Java source code for a category of demos, and a `native/` directory with the runtime artifacts. Each demo directory symlinks its `native/` folder to the corresponding module.

```
godot-java-demo-projects/
├── pom.xml                          # Parent POM
├── demos-2d/                        # Maven module
│   ├── src/main/java/demos/twod/    # Java source
│   └── native/                      # Runtime (JAR + dylib + gdextension)
│       ├── demos-2d.jar
│       ├── godot-java.gdextension
│       └── libgodot-java.dylib
├── demos-3d/
├── demos-gui-audio/
├── demos-misc-xr-cmp/
├── demos-net-vp-load/
├── 2d/pong/                         # Godot project
│   ├── native -> ../../demos-2d/native  # Symlink
│   ├── pong.tscn
│   └── project.godot
├── 3d/
├── audio/
├── gui/
...
```

All game logic is written in pure Java using `@GodotClass` annotations. The `.tscn` scene files reference Java class names directly as node types. There is zero GDScript game logic.

## Demo Categories

| Category | Module | Count |
|----------|--------|-------|
| 2D | demos-2d | 22 |
| 3D | demos-3d | 17 |
| Audio & GUI | demos-gui-audio | 18 |
| Networking, Viewport, Loading | demos-net-vp-load | 22 |
| Misc, XR, Compute | demos-misc-xr-cmp | 28 |

## Test Results

87/107 (81.3%) demos pass with zero errors. The remaining 20 failures are all engine-level issues (RID/resource leaks, XR runtime missing, theme bugs) — no Java code issues remain.

See [API_GAPS.md](API_GAPS.md) for documented godot-java API limitations encountered during porting.

## License

MIT License — same as the original [godot-demo-projects](https://github.com/godotengine/godot-demo-projects).
