package demos.threed.squash_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;

@GodotClass(name = "STCMob", parent = "CharacterBody3D")
public class STCMob extends CharacterBody3D {

	@Export
	public double minSpeed = 10.0;
	@Export
	public double maxSpeed = 18.0;

	private org.godot.Godot animPlayer;
	private org.godot.Godot notifier;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		call("add_user_signal", "squashed");
		animPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
		notifier = (org.godot.Godot) call("get_node", "VisibleOnScreenNotifier3D");

		if (notifier != null) {
			org.godot.core.Callable cb = new org.godot.core.Callable(this, "_on_screen_exited");
			notifier.connect("screen_exited", cb, 0);
		}
	}

	@Override
	public void _physicsProcess(double delta) {
		call("move_and_slide");
	}

	@GodotMethod
	public void initialize(Vector3 startPosition, Vector3 playerPosition) {
		// Look at player (ignoring height)
		Vector3 target = new Vector3(playerPosition.getX(), startPosition.getY(), playerPosition.getZ());
		call("look_at_from_position", startPosition, target, new Vector3(0, 1, 0));

		// Random rotation offset
		double randomAngle = (Math.random() - 0.5) * Math.PI / 2;
		call("rotate_y", randomAngle);

		// Random speed
		double randomSpeed = minSpeed + Math.random() * (maxSpeed - minSpeed);
		Vector3 forward = new Vector3(0, 0, -1); // FORWARD
		Vector3 velocity = new Vector3(forward.getX() * randomSpeed, 0, forward.getZ() * randomSpeed);

		// Rotate velocity by mob's Y rotation
		Vector3 rot = (Vector3) getProperty("rotation");
		double angle = rot != null ? rot.getY() : 0;
		double cosA = Math.cos(angle);
		double sinA = Math.sin(angle);
		double rx = velocity.getX() * cosA - velocity.getZ() * sinA;
		double rz = velocity.getX() * sinA + velocity.getZ() * cosA;
		setProperty("velocity", new Vector3(rx, velocity.getY(), rz));

		if (animPlayer != null) {
			animPlayer.setProperty("speed_scale", randomSpeed / minSpeed);
		}
	}

	@GodotMethod
	public void squash() {
		call("emit_signal", "squashed");
		call("queue_free");
	}

	@GodotMethod
	public void _on_screen_exited() {
		call("queue_free");
	}

	@Override
	public void _exitTree() {
		if (animPlayer != null) {
			animPlayer.call("stop");
			animPlayer = null;
		}
	}
}
