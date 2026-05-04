# godot-java-demo-projects

使用 [godot-java](https://github.com/youngledo/godot-java) 将 Godot 官方示例项目重写为纯 Java 版本。

本项目将 [godot-demo-projects](https://github.com/godotengine/godot-demo-projects)（原 GDScript 编写）移植为纯 Java，通过 GDExtension + Panama FFI 证明 Java 是 Godot 游戏开发的一等公民语言。

## 环境要求

- JDK 25+
- Apache Maven 4.0+
- Godot 4.6+
- godot-java 原生库（`libgodot-java.dylib` / `.so` / `.dll`）

## 构建

```bash
mvn package -DskipTests
```

构建产物为 fat JAR：`native/godot-java-demo-projects.jar`。

## 运行示例

1. 构建项目
2. 将原生库复制到 `native/` 目录
3. 在 Godot 编辑器中打开某个示例的 Godot 项目（如 `2d/pong/`）
4. 设置环境变量 `GODOT_JAVA_CLASSPATH` 指向 `native/godot-java-demo-projects.jar`
5. 运行项目（F5）

示例：

```bash
export GODOT_JAVA_CLASSPATH=/path/to/godot-java-demo-projects/native/godot-java-demo-projects.jar
godot --path 2d/pong/
```

## 架构

所有游戏逻辑使用 `@GodotClass` 注解以纯 Java 编写。`.tscn` 场景文件直接使用 Java 类名作为节点类型，零 GDScript 游戏逻辑。

```
godot-java-demo-projects/
├── pom.xml
├── native/                          # 共享运行时
│   ├── godot-java.gdextension
│   ├── godot-java-demo-projects.jar
│   └── libgodot-java.dylib
├── src/main/java/demos/             # Java 源码
│   ├── twod/
│   └── threed/
├── 2d/                              # Godot 项目（场景 + 资源）
└── 3d/
```

## 许可证

MIT License — 与原项目 [godot-demo-projects](https://github.com/godotengine/godot-demo-projects) 一致。
