package demos.twod.physics_platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.RigidBody2D;

@GodotClass(name = "PPBullet", parent = "RigidBody2D")
public class PPBullet extends RigidBody2D {

	public boolean disabled = false;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		org.godot.node.Timer timer = (org.godot.node.Timer) getNode("Timer");
		if (timer != null) timer.start();
	}

	@Override
	public void _exitTree() {
		org.godot.node.Timer timer = getNodeAs("Timer", org.godot.node.Timer.class);
		if (timer != null) timer.stop();
	}

	@GodotMethod
	public void disable() {
		if (disabled) return;
		org.godot.node.AnimationPlayer anim = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
		if (anim != null) anim.play("shutdown");
		disabled = true;
	}
}
