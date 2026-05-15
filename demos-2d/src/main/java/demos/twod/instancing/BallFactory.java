package demos.twod.instancing;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.Node2D;
import org.godot.singleton.Input;

@GodotClass(name = "BallFactory", parent = "Node2D")
public class BallFactory extends Node2D {

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		if (!Input.singleton().isActionJustPressed("click")) {
			return false;
		}
		spawn(getGlobalMousePosition());
		return true;
	}

	private void spawn(Vector2 spawnPos) {
		org.godot.node.PackedScene ballScene = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://ball.tscn");
		if (ballScene == null) {
			return;
		}
		if (ballScene.instantiate() instanceof Node2D instance) {
			instance.setGlobalPosition(spawnPos);
			addChild(instance);
		}
	}
}
