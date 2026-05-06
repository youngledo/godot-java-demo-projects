# 107 个 Demo 跑通之后：godot-java 不是玩具

上一篇[文章](https://mp.weixin.qq.com/s/BIJ4aESmizoHt6v3LRDzvA)，我介绍了 godot-java——一个基于 GDExtension + Panama FFI 的 Godot 4.x Java 绑定，目标是让 Java 在 Godot 生态中成为一等公民。

文章发出后，收到最多的反馈是：**框架介绍得挺好，但能写真正的游戏吗？是不是只能跑 Hello World？**

这篇文章就是来回答这个问题的。我们把 Godot 官方的 107 个 demo 全部用 Java 重写了一遍，又把 GDQuest 的一个完整第三人称射击游戏移植到了 Java。下面看结果和代码。

---

## 先说结论

**107 个官方 demo 移植结果：**

- 87 个零错误通过（81.3%）
- 剩余 20 个失败全部是引擎层面的问题（RID/资源泄漏、XR 运行时缺失、主题 bug），与 Java 代码无关
- 覆盖了 2D、3D、物理、粒子、导航、网络、GUI、音频、XR 等全部类别

**第三人称射击游戏移植结果：**

- 原版 26 个 GDScript 类全部转为 Java（27 个 Java 文件）
- 包含玩家控制、AI 敌人、金币系统、武器切换、UI 交互——一个真正意义上的完整游戏

这不是精心挑选的几个示例。Godot 官方 demo 仓库里有什么，我们就移植什么，逐一测试。

---

## 写一个 Pong 只需要一份 Java 文件

Pong 是 Godot 官方 demo 中最简单的 2D 项目。整个游戏就一个类，157 行：

```java
@GodotClass(name = "PongGame", parent = "Node2D")
public class PongGame extends Node2D {

    @Export
    public double paddleSpeed = 100.0;

    private ColorRect ball;
    private ColorRect leftPaddle;
    private ColorRect rightPaddle;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // 用 Java 创建所有节点——球、挡板、分隔线
        ColorRect bg = ColorRect.create();
        bg.call("set_color", new Color(0.14, 0.15, 0.16));
        bg.call("set_size", new Vector2(WIDTH, HEIGHT));
        add_child(bg, false, 0);

        ball = ColorRect.create();
        ball.call("set_color", new Color(1, 1, 1));
        ball.call("set_size", new Vector2(BALL_SIZE, BALL_SIZE));
        add_child(ball, false, 0);
        // ... 省略其余节点创建
    }

    @Override
    public void _process(double delta) {
        Input input = Input.singleton();
        // 键盘输入 → 挡板移动 → 碰撞检测 → 得分判定
        // 全部是纯 Java 逻辑
    }
}
```

注意几个要点：

- `@GodotClass` 把 Java 类注册为 Godot 节点类型，场景文件可以直接引用
- `@Export` 把字段暴露给编辑器 Inspector，像 GDScript 的 `@export` 一样用
- `ColorRect.create()` 动态创建节点，`add_child()` 添加到场景树
- `_ready()` 和 `_process()` 就是 Godot 的生命周期回调，写法与 GDScript 一一对应

**整个游戏没有任何 GDScript。** Godot 项目里只有 `.tscn` 场景文件和一个指向 Java 类的脚本引用。

---

## 3D 跟随相机：Godot 数学类型直接用

3D demo 更能体现 API 的完整度。这是一个跟随相机，处理了距离限制、高度钳制：

```java
@GodotClass(name = "PLFollowCamera", parent = "Camera3D")
public class PLFollowCamera extends Camera3D {

    @Override
    public void _physicsProcess(double delta) {
        org.godot.Godot parent = (org.godot.Godot) call("get_parent");
        Object targetObj = parent.call("get_global_transform");
        Object posObj = call("get_global_transform");

        Vector3 target = ((Transform3D) targetObj).getOrigin();
        Vector3 pos = ((Transform3D) posObj).getOrigin();
        Vector3 diff = pos.sub(target);

        // 距离钳制
        double dist = diff.length();
        if (dist < minDistance) diff = diff.normalized().mul(minDistance);
        else if (dist > maxDistance) diff = diff.normalized().mul(maxDistance);

        // 高度钳制
        diff = new Vector3(diff.x,
                Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, diff.y)),
                diff.z);

        pos = target.add(diff);
        call("look_at_from_position", pos, target, Vector3.UP);
    }
}
```

`Vector3`、`Transform3D`、`Basis` 这些 Godot 数学类型都有对应的 Java 封装。`sub()`、`add()`、`mul()`、`normalized()`、`length()`——不需要写额外的数学工具类。

---

## 信号、物理、碰撞检测：覆盖真实游戏场景

再看一个物理平台游戏中的金币拾取。34 行，涉及信号连接和类型判断：

```java
@GodotClass(name = "PPCoin", parent = "Area2D")
public class PPCoin extends Area2D {

    private boolean taken = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        connect("body_entered", new Callable(this, "_on_body_entered"), 0);
    }

    @GodotMethod
    public void _on_body_entered(Object body) {
        if (!taken) {
            org.godot.Godot b = (org.godot.Godot) body;
            String cls = (String) b.call("get_class");
            if ("PPPlayer".equals(cls)) {
                taken = true;
                org.godot.Godot anim = (org.godot.Godot) call("get_node", "AnimationPlayer");
                anim.call("play", "taken");
            }
        }
    }
}
```

逻辑清晰：连接 `body_entered` 信号 → 检查碰撞体类型 → 播放动画。Java 写 GDScript 能做的事，没有绕弯路。

---

## 完整游戏：第三人称射击 demo

上面的都是单个功能点的展示。这个第三人称射击 demo 是一个"真正的游戏"——改编自 GDQuest 的开源项目，27 个 Java 类：

| 功能模块 | Java 类 | 说明 |
|---------|---------|------|
| 玩家控制 | `Player`, `CameraController`, `CharacterSkin` | 跑、跳、射击、投掷、瞄准 |
| 武器系统 | `Bullet`, `Grenade`, `GrenadeLauncher`, `MeleeAttackArea` | 三种武器各有独立逻辑 |
| 敌人 AI | `Beebot`（飞行射击）, `Beetle`（地面追踪） | 导航网格 + 状态机 |
| 金币系统 | `Coin`, `CoinModel`, `CoinsContainer` | 物理生成 → 跟随玩家 → 收集计数 |
| 关卡元素 | `Box`, `DestroyedBox`, `JumpingPad`, `DeathPlane` | 可破坏箱子、弹跳台、死亡区域 |
| UI | `DemoPage`, `WeaponUI`, `LinkButton`, `CameraMode` | 暂停菜单、武器指示、全屏切换 |

其中玩家控制器用到了 godot-java 的全部核心特性：

- **9 个 `@Export` 属性**：移动速度、子弹速度、跳跃力度等，全部可在编辑器中调节
- **`@Signal` 信号声明**：`weapon_switched`，UI 响应武器切换
- **`@GodotMethod` 回调**：`resetPosition()`、`collectCoin()`、`playFootStepSound()` 供动画轨道调用
- **`_physicsProcess` 主循环**：输入处理、速度插值、碰撞响应、动画切换

敌人 AI 同样完整。`Beetle` 使用 `NavigationAgent3D` 做导航追踪，`Beebot` 在飞行中定时向玩家射击——这些都是 Godot 原生 API 的 Java 调用，不是模拟。

---

## 诚实地说：还有些不完美的地方

不回避问题。在移植过程中，我们也遇到了一些 API 覆盖不足的情况：

1. **部分节点方法需要 `call()` 转发**：`add_child()`、`get_parent()` 等常用方法目前通过字符串分发调用，还没有生成强类型的 Java 方法
2. **自定义绘制（`_draw`）缺失**：导致少量纯绘制类 demo 无法完整移植
3. **3D 的 `_integrateForces` 未完全支持**：2D 物理回调可用，3D 回退到 `_physicsProcess`
4. **没有 async/await**：GDScript 的 `await` 模式需要转为信号回调或状态机——这是语言差异而非框架缺陷

这些问题都已记录在 `API_GAPS.md` 中，会在后续版本逐步补齐。但重要的是：**这些问题没有阻止任何一类游戏 demo 的移植**。替代方案始终存在，游戏都能跑起来。

---

## 107 个 demo 意味着什么

很多语言绑定止步于"能跑一个 Hello World"。但游戏引擎的 API 深度非常夸张——Godot 4.6 有 1016 个引擎类、1144 个虚函数、176 个全局 API 函数。一个绑定框架要真正可用，必须覆盖足够广的面。

这 107 个 demo 本质上是一次**系统性的 API 压力测试**：

- 2D 物理（RigidBody2D、CollisionShape2D）✅
- 3D 物理（CharacterBody3D、RigidBody3D、NavigationAgent3D）✅
- 粒子系统（GPUParticles2D/3D、ProcessMaterial）✅
- GUI 控件（Button、ItemList、Tree、TabContainer）✅
- 音频（AudioStreamPlayer、AudioEffect）✅
- 着色器参数传递 ✅
- 多视口和渲染服务器 ✅
- 网络（WebSocket、ENetMultiplayer）✅
- XR（OpenXR 接口）✅

---

## 开发体验：在 IntelliJ IDEA 里写 Godot

因为 godot-java 就是普通的 Maven 项目，你可以：

- 在 IntelliJ IDEA 中打开，获得完整的代码补全、重构、跳转
- 用 Log4j2 做日志、JUnit 做单元测试
- 用 Maven 管理依赖和构建
- 享受 Java 25 的现代语言特性（record、sealed class、pattern matching）

修改 Java 代码 → `mvn package` → 回到 Godot 编辑器按 F5，这就是完整的开发循环。

---

## 链接

- **godot-java 框架**：[github.com/youngledo/godot-java](https://github.com/youngledo/godot-java)
- **107 个 demo**：[github.com/youngledo/godot-java-demo-projects](https://github.com/youngledo/godot-java-demo-projects)
- **第三人称射击 demo**：[github.com/youngledo/godot-java-3d-demo](https://github.com/youngledo/godot-java-3d-demo)

欢迎试用、反馈、贡献。
