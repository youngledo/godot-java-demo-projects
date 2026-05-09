# godot-java API Gaps Found During Demo Porting

## Current Status (2026-05-09)

The typed API migration for the main demo modules is complete, and the previously stubbed drawing/accessibility demos now have Java implementations instead of stale blocked placeholders.

| Area | Status |
|---|---|
| Demo Java source `call()` usage | 0 remaining in `demos-2d`, `demos-3d`, `demos-gui-audio`, `demos-misc-xr-cmp`, `demos-net-vp-load` |
| Generated wrapper `super.call(...)` | Fixed in `godot-java` codegen/runtime (`e0b81bb`) |
| Demo Java files | 276 in demo modules |
| Registered `@GodotClass` | 275 in demo modules |
| Previously stubbed demos | `2d/custom_drawing`, `gui/gd_paint`, and `gui/accessibility` now have Java code |
| Remaining tracked migration tasks | Completed |

Validation performed:

```text
rg -n "\bcall\(|\.call\(" demos-2d/src/main/java demos-3d/src/main/java demos-gui-audio/src/main/java demos-misc-xr-cmp/src/main/java demos-net-vp-load/src/main/java --glob '*.java'
# no output

mvn compile -pl demos-2d,demos-gui-audio -am
# BUILD SUCCESS

grep -R "super.call" godot-java-core/target/generated-sources/codegen godot-java-code-generator/src/main/java
# no output after forced regeneration in godot-java
```

## Previously Blocked or Stubbed Demos

These are no longer blocked by missing Java `_draw()` support.

| Demo | Current Status | Notes |
|---|---|---|
| `2d/custom_drawing` | Java port added | `CustomDrawingCanvas.java` uses typed CanvasItem draw APIs for lines, rectangles, polygons, text, animation, and placeholders for unsupported-heavy tabs. |
| `gui/gd_paint` | Java port added | `PaintControl.java` handles mouse drawing with pencil, eraser, rectangle, circle, brush shape, brush size, color, background, undo, and clear; `ToolsPanel.java` uses typed UI nodes and scene signal handlers. |
| `gui/accessibility` | Java port added | `Controls.java` uses typed `LineEdit`/`Label`; `CustomControl.java` implements a drawn, keyboard/mouse-interactive custom control. Framework-level accessibility notification mapping remains a non-blocking follow-up. |

## Resolved by Codegen / Runtime Fixes

The following gaps were found during porting and are now resolved or no longer blocking the migrated demos.

| Previously Missing or Blocking | Current Status | Notes |
|---|---|---|
| `Node.getParent()`, `getTree()`, `getViewport()`, `getPath()` | Generated | Typed methods available |
| `Node.setProcess()`, `setPhysicsProcess()`, `setProcessInput()` | Generated | Typed methods available |
| `Node.addChild(node)`, `getChild(idx)` default-param overloads | Generated | Default argument overload generation works |
| `FileAccess.open()`, `fileExists()` and other static APIs | Generated | Static method generation uses `callStatic` / method-bind dispatch |
| `CharacterBody2D/3D` movement APIs | Generated | `moveAndSlide()`, velocity/floor helpers available |
| `CanvasItem._draw()` override | Available | `BSBullets.java`, `CustomDrawingCanvas.java`, `PaintControl.java`, and `CustomControl.java` use it successfully |
| `CanvasItem.show()`, `hide()`, visibility setters | Generated | Typed methods available |
| `Control`, `Label`, `ColorRect`, theme override setters | Generated | Typed property/method wrappers available |
| `SurfaceTool.setUv()`, `setNormal()` | Generated | Voxel demo uses typed methods |
| `Godot.callDeferred()` | Added | Base helper remains for deferred method names |
| Generated wrapper instance dispatch | Fixed | `JavaClassGenerator` emits `callEngine(...)`, not `super.call(...)` |

## Remaining API Limitations and Notes

These items are still worth tracking, but they are not blocking the completed `call()` migration or the Java drawing demos.

### Virtual / Notification Coverage

| Area | Status | Notes |
|---|---|---|
| `_draw()` | Available | Multiple Java demos now override `_draw()` directly |
| Accessibility notifications | Needs follow-up | `gui/accessibility` has a Java visual/interactive port, but framework-level `NOTIFICATION_ACCESSIBILITY_*` constants and screen-reader behavior still need proper Java mapping |
| GDScript `await` pattern | No direct equivalent | Continue using signal callbacks, timers, polling, or explicit state machines |

### Transform / Math Convenience APIs

| API Area | Current Use |
|---|---|
| `Transform3D.lookingAt()` | Some IK demos still use local `makeLookAtBasis()` helpers |
| `Transform3D.apply()` / inverse apply | Used as typed replacement for old `xform` / `xformInv` style |
| `Basis.toEuler()` | Returns `Vector3`; callers use vector components directly |

### Naming / Type Notes

| Area | Note |
|---|---|
| `org.godot.node.Object` vs `java.lang.Object` | Use `java.lang.Object` when disambiguation is needed |
| Custom `@GodotClass.create()` | Generated factory returns generated parent wrapper type; custom subclasses should use scene instantiation or project-specific factories |

## Out of Scope for This File

`/Users/huangxiao/Workspace/mine/godot-java-3d-demo` is a separate project and is not covered by the completed `godot-java-demo-projects` migration. It still has dynamic `call()` usages and needs a separate migration pass.
