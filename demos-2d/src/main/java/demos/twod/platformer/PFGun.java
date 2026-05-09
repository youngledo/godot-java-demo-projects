package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Marker2D;
import org.godot.node.RigidBody2D;

@GodotClass(name = "PFGun", parent = "Marker2D")
public class PFGun extends Marker2D {

	private static final double BULLET_VELOCITY = 850.0;
	private org.godot.node.Timer cooldownTimer;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		cooldownTimer = getNodeAs("Cooldown", org.godot.node.Timer.class);
	}

	@GodotMethod
	public boolean shoot(double direction) {
		if (cooldownTimer != null && !(boolean) cooldownTimer.isStopped()) {
			return false;
		}

		org.godot.node.PackedScene bulletSceneObj = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://player/bullet.tscn");
		if (bulletSceneObj == null) return false;
		if (!(bulletSceneObj.instantiate() instanceof RigidBody2D bullet)) return false;

		org.godot.math.Vector2 pos = getGlobalPosition();
		if (pos != null) bullet.setGlobalPosition(pos);

		bullet.setLinearVelocity(new org.godot.math.Vector2(direction * BULLET_VELOCITY, 0));
		bullet.setAsTopLevel(true);

		addChild(bullet);

		if (cooldownTimer != null) cooldownTimer.start();
		return true;
	}

	@Override
	public void _exitTree() {
		if (cooldownTimer != null) cooldownTimer.stop();
		cooldownTimer = null;
	}
}
