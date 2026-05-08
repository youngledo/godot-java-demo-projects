# Plan: 全面消除 call() — 类型化 API 迁移

## Context

call() 从 2817 → 1201（完成率 57.3%）。Phase 2/3 已完成，Phase 4 部分完成。

## 当前剩余 1201 个 call() 的精确分布

| 类别 | 数量 | 难度 | 策略 |
|------|------|------|------|
| Singleton 访问 `call("OS.method")` | 106 | 低 | 替换为 `OS.singleton().method()` |
| load() 自调用 | 14 | 低 | 替换为 `ResourceLoader.singleton().load()` |
| Animation play/start/stop/disconnect | 52 | 中 | 升级 AnimatedSprite2D、AnimationPlayer 等类型 |
| Tween 调用 | 8 | 中 | 检查 Tween 有无 tween_property 类型方法 |
| FSM 状态模式 `currentState.call("enter"/"exit"/"handle_input")` | 11 | 中 | 创建 FSState 接口 |
| Dictionary/Array 引擎调用 | 29 | 中 | 检查 GodotDictionary 等类型 |
| WebSocket/WebRTC | 213 | 高 | 检查 WebSocketPeer/StreamPeer 生成类 |
| XR 相关 | 355 | 高 | 检查 XRTracker/XRInterface 生成类 |
| 其他引擎 API（各种 Godot 变量） | ~413 | 高 | 混合原因，逐文件分析 |

## Phase 4A: 低难度快速消除（128 个）

**目标**: 消除 Singleton(106) + load(14) + Tween(8) 共约 128 个 call()。

### 4A-1: Singleton 访问替换（106 个）

**模式**: `call("OS.get_process_id")` → `org.godot.singleton.OS.singleton().getProcessId()`

**涉及单例**: OS, Engine, Time, AudioServer, ProjectSettings, ClassDB, JSON, Input, DisplayServer, IP, Performance, PhysicsServer, RenderingServer, ResourceLoader, SceneTree, TranslationServer, ThemeDB

**操作**:
1. 检查每个单例的 typed 方法是否存在及签名是否匹配
2. 签名匹配的 → 直接替换
3. 签名不匹配的 → 保留 `.call()` 并记录到 API_GAPS.md

**关键文件**（按数量排序）:
- `demos-misc-xr-cmp/.../os_test/OSTest.java` (~22 calls)
- `demos-misc-xr-cmp/.../os_test/Actions.java` (~36 calls)
- `demos-gui-audio/.../audio/generator/GeneratorDemo.java`
- `demos-gui-audio/.../audio/mic_record/MicRecord.java`
- 其他使用单例的文件

**已知签名问题**（需保留 .call()）:
- `DisplayServer.windowSetPosition(Vector2)` → 应接受 `Vector2i`
- `DisplayServer.windowSetSize(Vector2)` → 应接受 `Vector2i`
- `OS.getConnectedMidiInputs()` → 返回类型不匹配
- `DisplayServer.ttsSpeak()` → 参数签名不匹配

### 4A-2: load() 自调用替换（14 个）

**模式**: `call("load", "res://path")` → `org.godot.singleton.ResourceLoader.singleton().load("res://path")`

**涉及文件**:
- `MPBomberGameState.java` (2)
- `MPBomberBombSpawner.java` (1)
- `MPPongLobby.java` (1)
- `WebRTCMinimalMain.java` (1)
- `PickupAbleBody.java` (1)
- `WaterPlane.java` (2)
- `BeachCave.java` (1)
- `PostProcessGrayscale.java` (1)
- `HeightmapMain.java` 等

**注意**: `call("load", ...)` 返回 Object，`ResourceLoader.load()` 也返回 Object。直接替换即可。

### 4A-3: Tween 类型方法替换（8 个）

**模式**: `tween.call("tween_property", ...)` → 检查 Tween 是否有 `tweenProperty()` 方法

**涉及文件**:
- `TweenDemo.java`
- `RPWalker.java`
- `OXCLMain.java`
- `HeightmapMain.java`

**验证**: `mvn compile` 全部模块通过

## Phase 4B: Animation 类型升级（52 个）

**目标**: 消除 Animation 相关 call()。

### 4B-1: AnimatedSprite2D play/stop

**变量**: `pose`（在 RPWalker 等文件中为 `org.godot.node.Node` 类型）

**操作**: 升级为 `org.godot.node.AnimatedSprite2D`，替换 `.call("play", name)` → `.play(name)`

### 4B-2: AnimationNodeStatePlayback start

**变量**: `playback`（从 `animationTree.get("parameters/playback")` 获取）

**问题**: AnimationNodeStatePlayback 可能未在 codegen 中生成。需检查。

### 4B-3: disconnect 调用

**模式**: `animPlayer.call("disconnect", signal, callable)` → `animPlayer.disconnect(signal, callable)`

**已知**: `Object.disconnect(String, Callable)` 已生成

**验证**: `mvn compile` 全部模块通过

## Phase 4C: FSM 接口设计（11 个）

**目标**: 为 FSM 状态模式创建公共接口。

**当前问题**: `currentState` 类型为 `Godot`，无法调用 `enter()`/`exit()`/`handleInput()`。

**方案**: 创建 `FSState` 接口：
```java
public interface FSState {
    void enter();
    void exit();
    boolean handleInput(Object inputEvent);
}
```

让所有状态类（FSIdle, FSMove, FSJump, FSAttack, FSStagger, FSDie）实现此接口。

将 `currentState` 从 `Godot` 改为 `FSState`。

**涉及文件**:
- 新建: `FSState.java`（接口）
- 修改: FSIdle, FSMove, FSJump, FSAttack, FSStagger, FSDie（implements FSState）
- 修改: FSStateMachine, FSPlayerStateMachine（currentState 类型改为 FSState）

**验证**: `mvn compile` 全部模块通过

## Phase 4D: Dictionary/Array 引擎调用（29 个）

**目标**: 消除 Dictionary/Array 上的 `.call()` 调用。

**模式**: `dict.call("keys")`, `dict.call("get", key)`, `arr.call("push_back", item)`

**操作**: 检查是否有 GodotDictionary/GodotArray typed wrapper，或使用 `getProperty`/`setProperty` 替代。

## Phase 4E: WebSocket/WebRTC 类型升级（213 个）

**目标**: 升级 WebSocket 和 WebRTC 相关变量类型。

**前置条件**: 检查以下类型是否在 codegen 中生成：
- `WebSocketPeer`
- `StreamPeer`
- `StreamPeerTCP`
- `TCPServer`
- `PacketPeer`
- `ENetMultiplayerPeer`
- `WebRTCPeerConnection`
- `MultiplayerPeer`

**如果类型已生成**: 直接升级变量类型并替换 `.call()`

**如果类型未生成**: 记录到 API_GAPS.md，作为 codegen 补充项

**涉及文件**:
- `WSChatWebSocketClient.java` / `WSChatWebSocketServer.java`
- `WSMinimalServer.java`
- `WSMPMain.java` / `WSMPGame.java`
- `WebRTCSignaling*.java`
- `WebRTCMinimal*.java`

## Phase 4F: XR 类型升级（355 个）

**目标**: 升级 XR 相关变量类型。

**前置条件**: 检查以下类型是否在 codegen 中生成：
- `XRInterface`
- `XRTracker`
- `XRPose`
- `XRController3D`
- `XROrigin3D`

**如果类型已生成**: 直接升级变量类型并替换 `.call()`

**如果类型未生成**: 记录到 API_GAPS.md

**涉及文件**:
- `OXCCPlayer.java`, `OXOCPlayer.java`
- `CollisionHands.java`
- `OXCLMain.java`
- 其他 XR demo 文件

## Phase 5: 单例方法签名修复（框架侧）

**目标**: 在 godot-java codegen 中修复单例方法的参数/返回类型不匹配问题。

**已知问题**:
- `DisplayServer.windowSetPosition(Vector2)` → 应接受 `Vector2i`
- `DisplayServer.windowSetSize(Vector2)` → 应接受 `Vector2i`
- `OS.getConnectedMidiInputs()` → 返回 `String[]` 非 `GodotArray`
- `ProjectSettings.globalizePath()` → 返回 `Object` 非 `String`
- `DisplayServer.ttsSpeak()` → 参数签名不匹配

**操作**: 在 godot-java codegen 中修复签名，重新生成

**验证**: 修复后替换所有单例 `.call()` 为类型化方法

## Phase 6: 最终审计

**操作**:
1. 统计剩余 `call()` 数量
2. 逐个确认每个剩余 `call()` 无法消除的原因
3. `mvn compile` 全部模块通过
4. 更新 API_GAPS.md

## 执行顺序

```
Phase 4A (快速消除 ~128) ──→ Phase 4B (Animation ~52)
                            ──→ Phase 4C (FSM 接口 ~11)
                            ──→ Phase 4D (Dict/Array ~29)
Phase 4E (WebSocket ~213) ── 需先检查 codegen 类型是否存在
Phase 4F (XR ~355) ── 需先检查 codegen 类型是否存在
Phase 5 (单例签名) ── 框架侧修改
Phase 6 (审计) ── 最终
```

## 关键文件

- Codegen: `/Users/huangxiao/Workspace/mine/godot-java/godot-java-code-generator/src/main/java/com/godot/codegen/JavaClassGenerator.java`
- 生成的 Node.java: `/Users/huangxiao/Workspace/mine/godot-java/godot-java-core/target/generated-sources/codegen/org/godot/node/Node.java`
- Demo 源码: `/Users/huangxiao/Workspace/mine/godot-java-demo-projects/*/src/main/java/`
