# godot-java API Gaps Found During Demo Porting

## Blocked Demos

### 2d/custom_drawing (BLOCKED)
- **`_draw()` virtual method not available** — CanvasItem's `_draw()` cannot be overridden in Java.
  This blocks all custom drawing demos (9 scripts, 649 lines).

## API Limitations & Workarounds

### Virtual Methods
| Virtual Method | Status | Workaround |
|---|---|---|
| `_draw()` | Not available | BLOCKED — no workaround |
| `_integrateForces()` | Not available | Use `_physicsProcess()` instead for physics_platformer |

### Transform3D API
| Missing API | Workaround |
|---|---|
| `Transform3D.lookingAt()` | Manual `makeLookAtBasis()` helper with cross products |
| `Transform3D.xform()` | Use `apply()` |
| `Transform3D.xformInv()` | Use `inverse().apply()` |

### Basis API
| Missing API | Workaround |
|---|---|
| `Basis.getColumn(i)` | Access fields directly: `xx,xy,xz,yx,yy,yz,zx,zy,zz` |
| `Basis.rotated()` | Use `Basis.fromAxisAngle(axis, angle).multiply(basis)` |
| `Basis.toEuler()` returns | Returns `Vector3` (not `double[]`) — use `.x`, `.y`, `.z` |

### Other API Gaps
| Missing API | Workaround |
|---|---|
| `Vector3.floor()` | Use `new Vector3(Math.floor(x), Math.floor(y), Math.floor(z))` |
| `Vector2i.ZERO` | Use `new Vector2i(0, 0)` |
| `SurfaceTool.set_uv()` | Use `surfaceTool.call("set_uv", ...)` |
| `SurfaceTool.set_normal()` | Use `surfaceTool.call("set_normal", ...)` |
| `Node.add_child(node)` | Use `call("add_child", node)` or `add_child(node, false, 0)` |
| `Node.set_process()` | Use `call("set_process", bool)` |
| `Node.get_parent()` | Use `call("get_parent")` |
| `Node.call_deferred()` | Use `call("call_deferred", ...)` |
| `FileAccess.file_exists()` | Use `call("FileAccess.file_exists", path)` (instance method, not static) |
| `FileAccess.open()` | Use `call("FileAccess.open", path, mode)` |
| `_input()` returns `boolean` | Must return `boolean`, not `void` |
| `Object` naming conflict | Use `java.lang.Object` to disambiguate from `org.godot.node.Object` |
| Custom `@GodotClass.create()` | Returns parent type, not subclass — must refactor to use parent type or static methods |

### Async/Await Pattern
GDScript's `await` has no direct equivalent. All async patterns converted to:
- Signal callback chains (connect signal → handler calls next step)
- Timer-based polling
- Multi-step state machines

## Demo Stats

- **Total Java files:** 134
- **Registered @GodotClass:** 133
- **Demo directories:** 46 (2D + 3D + hello)
- **Blocked:** 1 demo (2d/custom_drawing)
