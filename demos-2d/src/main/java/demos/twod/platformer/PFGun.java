package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Marker2D;
import org.godot.node.Node;

@GodotClass(name = "PFGun", parent = "Marker2D")
public class PFGun extends Marker2D {

	private static final double BULLET_VELOCITY = 850.0;
	private org.godot.node.Node cooldownTimer;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		cooldownTimer = getNode("Cooldown");
	}

	@GodotMethod
	public boolean shoot(double direction) {
		if (cooldownTimer != null && !(boolean) cooldownTimer.call("is_stopped")) {
			return false;
		}

		org.godot.node.PackedScene bulletSceneObj = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://player/bullet.tscn");
		if (bulletSceneObj == null) return false;
		org.godot.Godot bullet = bulletSceneObj.instantiate();

		org.godot.math.Vector2 pos = (org.godot.math.Vector2) getProperty("global_position");
		if (pos != null) bullet.setProperty("global_position", pos);

		bullet.setProperty("linear_velocity", new org.godot.math.Vector2(direction * BULLET_VELOCITY, 0));
		bullet.call("set_as_top_level", true);

		addChild((org.godot.node.Node) bullet);

		if (cooldownTimer != null) cooldownTimer.call("start");
		return true;
	}

	@Override
	public void _exitTree() {
		if (cooldownTimer != null) cooldownTimer.call("stop");
		cooldownTimer = null;
	}
}
