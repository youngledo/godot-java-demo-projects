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

		org.godot.Godot timer = (org.godot.Godot) call("get_node", "Timer");
		if (timer != null) timer.call("start");
	}

	@Override
	public void _exitTree() {
		org.godot.Godot timer = (org.godot.Godot) call("get_node_or_null", "Timer");
		if (timer != null) timer.call("stop");
	}

	@GodotMethod
	public void disable() {
		if (disabled) return;
		org.godot.Godot anim = (org.godot.Godot) call("get_node", "AnimationPlayer");
		if (anim != null) anim.call("play", "shutdown");
		disabled = true;
	}
}
