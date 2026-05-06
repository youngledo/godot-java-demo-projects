package demos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.godot.annotation.GodotClass;
import org.godot.node.Node2D;
import org.godot.node.Node;

@GodotClass(name = "HelloDemo", parent = "Node2D")
public class HelloDemo extends Node2D {

    private static final Logger logger = LogManager.getLogger(HelloDemo.class);

    @Override
    public void _ready() {
        logger.info("Hello from godot-java-demo-projects! Java + Godot = <3");
    }
}
