package demos.twod.instancing;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.Node;
import org.godot.node.Node2D;
import org.godot.singleton.Input;

@GodotClass(name = "BallFactory", parent = "Node2D")
public class BallFactory extends Node2D {

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		String eventType = (String) call("get_class");
		// inputEvent is a Godot object with call() available through the wrapper
		// But inputEvent comes as a raw Object, need to check type via godot
		if (!(boolean) Input.singleton().isActionJustPressed( "click")) {
			return false;
		}
		Vector2 pos = (Vector2) call("get_global_mouse_position");
		spawn(pos);
		return true;
	}

	private void spawn(Vector2 spawnPos) {
		org.godot.node.PackedScene ballScene = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://ball.tscn");
		if (ballScene == null) {
			return;
		}
		Object instance = ballScene.instantiate();
		if (instance != null) {
			((org.godot.Godot) instance).call("set_global_position", spawnPos);
			addChild((Node) instance, false, 0);
		}
	}
}
