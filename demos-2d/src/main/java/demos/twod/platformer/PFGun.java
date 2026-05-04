package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Marker2D;

@GodotClass(name = "PFGun", parent = "Marker2D")
public class PFGun extends Marker2D {

	private static final double BULLET_VELOCITY = 850.0;
	private org.godot.Godot cooldownTimer;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		cooldownTimer = (org.godot.Godot) call("get_node", "Cooldown");
	}

	@GodotMethod
	public boolean shoot(double direction) {
		if (cooldownTimer != null && !(boolean) cooldownTimer.call("is_stopped")) {
			return false;
		}

		Object bulletSceneObj = call("load", "res://player/bullet.tscn");
		if (bulletSceneObj == null) return false;
		org.godot.Godot bullet = (org.godot.Godot) ((org.godot.Godot) bulletSceneObj).call("instantiate");

		org.godot.math.Vector2 pos = (org.godot.math.Vector2) getProperty("global_position");
		if (pos != null) bullet.setProperty("global_position", pos);

		bullet.setProperty("linear_velocity", new org.godot.math.Vector2(direction * BULLET_VELOCITY, 0));
		bullet.call("set_as_top_level", true);

		call("add_child", bullet);

		if (cooldownTimer != null) cooldownTimer.call("start");
		return true;
	}

	@Override
	public void _exitTree() {
		if (cooldownTimer != null) cooldownTimer.call("stop");
		cooldownTimer = null;
	}
}
