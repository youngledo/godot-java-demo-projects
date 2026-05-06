# godot-java API Gaps Found During Demo Porting

## Blocked Demos

### 2d/custom_drawing (BLOCKED — not ported)
- **`_draw()` virtual method not available** — CanvasItem's `_draw()` cannot be overridden in Java.
  This blocks all custom drawing demos (9 scripts, 649 lines). No Java files exist.

### gui/gd_paint (BLOCKED — stub only)
- `PaintControl.java` is a stub — depends on `_draw()` for rendering brush strokes.

### gui/accessibility (BLOCKED — stub only)
- `CustomControl.java` and `Controls.java` are stubs — depend on `_draw()` for visual rendering.

## Resolved by Codegen Fixes (2026-05-06)

The following API gaps were caused by code generator bugs and are now resolved:

| Previously Missing | Now Available | Notes |
|---|---|---|
| `Node.getParent()`, `getTree()`, `getViewport()`, `getPath()` | Generated | Fix: only skip get_/set_ when property exists |
| `Node.setProcess()`, `setPhysicsProcess()`, `setProcessInput()` | Generated | Same fix |
| `Node.addChild(node)` (1-arg) | Overload generated | Default param overloads from JSON |
| `Node.getChild(idx)` (1-arg) | Overload generated | Same |
| `FileAccess.open()`, `fileExists()` (static) | Generated as `public static` | Static method generation via `callStatic` |
| `DirAccess.open()`, `AudioStreamMP3.load_from_file()` | Generated as `public static` | Same |
| `Vector3.floor()`, `ceil()`, `normalized()` | `Vector3Extensions` companion class | Builtin method generation via FFI |
| 990+ methods with default parameters | Overloads generated | Read from extension_api.json |
| `CharacterBody2D.moveAndSlide()`, `isOnFloor()`, `getVelocity()`, `setVelocity()` | Generated | Physics methods on CharacterBody2D |
| `CanvasItem.show()`, `hide()` | Generated | Visibility methods |
| `ColorRect.setColor()`, `Control.setSize()`, `Control.setPosition()` | Generated | Property setters on Control subclasses |
| `Label.setText()`, `setHorizontalAlignment()` | Generated | Label property setters |
| `Control.addThemeFontSizeOverride()` | Generated | Theme override method |
| `Control.setVisible()` | Generated (on CanvasItem) | Visibility property setter |
| `Godot.callDeferred()` | Added to Godot base class | Deferred method calls |

## Remaining API Limitations & Workarounds

### Virtual Methods
| Virtual Method | Status | Notes |
|---|---|---|
| `_draw()` | Partially available | `BSBullets.java` works; `custom_drawing`, `gd_paint`, `accessibility` still blocked |
| `_integrateForces()` | Available | Fixed in commit `51cf035` — `PPEnemy.java` now uses proper override |

### Transform3D API
| Missing API | Workaround | Occurrences |
|---|---|---|
| `Transform3D.lookingAt()` | Manual `makeLookAtBasis()` helper with cross products | 7 |
| `Transform3D.xform()` | Use `apply()` | 11 |
| `Transform3D.xformInv()` | Use `inverse().apply()` | 2 |

### Basis API
| Missing API | Workaround |
|---|---|
| `Basis.getColumn(i)` | Access fields directly: `xx,xy,xz,yx,yy,yz,zx,zy,zz` |
| `Basis.rotated()` | Use `Basis.fromAxisAngle(axis, angle).multiply(basis)` |
| `Basis.toEuler()` returns | Returns `Vector3` (not `double[]`) — use `.x`, `.y`, `.z` |

### Other API Gaps
| Missing API | Workaround | Occurrences |
|---|---|---|
| `Vector3.floor()` | Use `new Vector3(Math.floor(x), Math.floor(y), Math.floor(z))` | 23 |
| `Vector2i.ZERO` | Use `new Vector2i(0, 0)` | 3 |
| `SurfaceTool.set_uv()` | Use `surfaceTool.call("set_uv", ...)` | 7 |
| `SurfaceTool.set_normal()` | Use `surfaceTool.call("set_normal", ...)` | 1 |
| `_input()` returns `boolean` | Must return `boolean`, not `void` | — |
| `Object` naming conflict | Use `java.lang.Object` to disambiguate from `org.godot.node.Object` | — |
| Custom `@GodotClass.create()` | Returns parent type, not subclass — must refactor to use parent type or static methods | — |

### Async/Await Pattern
GDScript's `await` has no direct equivalent. All async patterns converted to:
- Signal callback chains (connect signal -> handler calls next step)
- Timer-based polling
- Multi-step state machines

## call() Usage Statistics

### Current State (2026-05-07)

| Metric | Count |
|---|---|
| Total `call()` patterns in demos | 2,500 |
| Self-call() (on `this`) | 145 |
| Variable.call() (on Godot/other vars) | 2,003 |
| Typed method calls (replaced) | 1,041 |
| Node-typed variables | 507 |
| Godot-typed variables | 402 |

### godot-java-examples (2026-05-07)
All examples use typed API exclusively — zero `call()` patterns remaining.

### Top Remaining call() Categories (demos)

| Godot Method | Count | Why Still call() |
|---|---|---|
| `connect` | 123 | Signal connections on Godot-typed vars |
| `is_action_pressed` | 111 | Input singleton uses call() |
| `play` / `stop` / `start` | 113 | AnimationPlayer/AudioStreamPlayer methods |
| `instantiate` | 47 | PackedScene instantiation |
| `get_class` | 45 | Runtime type checking |
| `show` / `hide` | 76 | On Godot-typed vars (not typed as CanvasItem) |
| `get_node` | 41 | On Godot-typed vars |
| `set_rotation` | 38 | Property setter on Godot-typed vars |
| `get_global_transform/position` | 60 | On Godot-typed vars |
| `add_child` | 35 | On Godot-typed vars |

### Further call() Reduction Strategy
Most remaining `call()` patterns are on `org.godot.Godot`-typed variables. To reduce further:
1. Change variable types from `Godot` to more specific subclasses (e.g., `AnimationPlayer`, `AudioStreamPlayer`)
2. Use `Node` type where possible (507 vars already converted)
3. Add more typed methods to commonly-used classes via codegen

## Demo Stats

- **Total Java files:** 274
- **Registered @GodotClass:** 273
- **Demo directories:** 103
- **Blocked:** 3 demos (2d/custom_drawing, gui/gd_paint, gui/accessibility)
