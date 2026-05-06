package demos.threed.squash_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;
import org.godot.node.Node;

@GodotClass(name = "STCMob", parent = "CharacterBody3D")
public class STCMob extends CharacterBody3D {

	@Export
	public double minSpeed = 10.0;
	@Export
	public double maxSpeed = 18.0;

	private org.godot.node.AnimationPlayer animPlayer;
	private org.godot.node.VisibleOnScreenNotifier3D notifier;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		call("add_user_signal", "squashed");
		animPlayer = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
		notifier = (org.godot.node.VisibleOnScreenNotifier3D) getNode("VisibleOnScreenNotifier3D");

		if (notifier != null) {
			org.godot.core.Callable cb = new org.godot.core.Callable(this, "_on_screen_exited");
			notifier.connect("screen_exited", cb);
		}
	}

	@Override
	public void _physicsProcess(double delta) {
		moveAndSlide();
	}

	@GodotMethod
	public void initialize(Vector3 startPosition, Vector3 playerPosition) {
		// Look at player (ignoring height)
		Vector3 target = new Vector3(playerPosition.getX(), startPosition.getY(), playerPosition.getZ());
		lookAtFromPosition(startPosition, target, new Vector3(0, 1, 0));

		// Random rotation offset
		double randomAngle = (Math.random() - 0.5) * Math.PI / 2;
		rotateY(randomAngle);

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
		emitSignal("squashed");
		queueFree();
	}

	@GodotMethod
	public void OnScreenExited() {
		queueFree();
	}

	@Override
	public void _exitTree() {
		if (animPlayer != null) {
			animPlayer.stop();
			animPlayer = null;
		}
	}
}
