package demos.threed.rigidbody_character;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "RBCLevel", parent = "Node3D")
public class RBCLevel extends Node3D {

	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		org.godot.node.Node spawnTimer = getNode("SpawnTimer");
		if (spawnTimer != null) {
			spawnTimer.connect("timeout", new org.godot.core.Callable(this, "_on_spawn_timer_timeout"), 0);
		}

		org.godot.node.Node princess = getNode("Princess");
		org.godot.node.Node cubio = getNode("Cubio");
		if (princess != null && cubio != null) {
			princess.connect("body_entered", new org.godot.core.Callable(cubio, "_on_tcube_body_entered"), 0);
		}
	}

	@GodotMethod
	public void OnSpawnTimerTimeout() {
		org.godot.node.PackedScene cubeScene = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://cube_rigidbody.tscn");
		if (cubeScene == null) return;
		org.godot.Godot newRb = cubeScene.instantiate();
		if (newRb != null) {
			newRb.setProperty("position", new Vector3(
				(Math.random() - 0.5) * 10, 15, (Math.random() - 0.5) * 10));
			addChild((org.godot.node.Node) newRb);
		}
	}
}
