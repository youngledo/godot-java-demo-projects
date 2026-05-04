package demos.threed.rigidbody_character;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.Node3D;

@GodotClass(name = "RBCLevel", parent = "Node3D")
public class RBCLevel extends Node3D {

	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		org.godot.Godot spawnTimer = (org.godot.Godot) call("get_node", "SpawnTimer");
		if (spawnTimer != null) {
			spawnTimer.call("connect", "timeout", new org.godot.core.Callable(this, "_on_spawn_timer_timeout"));
		}

		org.godot.Godot princess = (org.godot.Godot) call("get_node", "Princess");
		org.godot.Godot cubio = (org.godot.Godot) call("get_node", "Cubio");
		if (princess != null && cubio != null) {
			princess.call("connect", "body_entered", new org.godot.core.Callable(cubio, "_on_tcube_body_entered"));
		}
	}

	@GodotMethod
	public void _on_spawn_timer_timeout() {
		Object cubeScene = call("load", "res://cube_rigidbody.tscn");
		if (cubeScene == null) return;
		org.godot.Godot newRb = (org.godot.Godot) ((org.godot.Godot) cubeScene).call("instantiate");
		if (newRb != null) {
			newRb.setProperty("position", new Vector3(
				(Math.random() - 0.5) * 10, 15, (Math.random() - 0.5) * 10));
			call("add_child", newRb);
		}
	}
}
