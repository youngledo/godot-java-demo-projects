package demos.twod.instancing;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.Node;
import org.godot.node.Node2D;

@GodotClass(name = "BallFactory", parent = "Node2D")
public class BallFactory extends Node2D {

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		String eventType = (String) call("get_class");
		// inputEvent is a Godot object with call() available through the wrapper
		// But inputEvent comes as a raw Object, need to check type via godot
		if (!call("Input.is_action_just_pressed", "click").equals(true)) {
			return false;
		}
		Vector2 pos = (Vector2) call("get_global_mouse_position");
		spawn(pos);
		return true;
	}

	private void spawn(Vector2 spawnPos) {
		Object ballScene = call("load", "res://ball.tscn");
		if (ballScene == null) {
			return;
		}
		Object instance = ((org.godot.Godot) ballScene).call("instantiate");
		if (instance != null) {
			((org.godot.Godot) instance).call("set_global_position", spawnPos);
			add_child((Node) instance, false, 0);
		}
	}
}
