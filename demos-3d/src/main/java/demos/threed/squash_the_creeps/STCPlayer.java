package demos.threed.squash_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;
import org.godot.singleton.Input;
import org.godot.node.Node;

@GodotClass(name = "STCPlayer", parent = "CharacterBody3D")
public class STCPlayer extends CharacterBody3D {

	@Export
	public double speed = 14.0;
	@Export
	public double jumpImpulse = 20.0;
	@Export
	public double bounceImpulse = 16.0;
	@Export
	public double fallAcceleration = 75.0;

	private org.godot.node.AnimationPlayer animPlayer;
	private org.godot.node.Node mobDetector;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		call("add_user_signal", "hit");
		animPlayer = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
		mobDetector = getNode("MobDetector");

		if (mobDetector != null) {
			mobDetector.connect("body_entered", new Callable(this, "_on_mob_detector_body_entered"), 0);
		}
	}

	@Override
	public void _physicsProcess(double delta) {
		Input input = Input.singleton();
		double dx = 0, dz = 0;

		if ((boolean) input.isActionPressed("move_right")) dx += 1;
		if ((boolean) input.isActionPressed("move_left")) dx -= 1;
		if ((boolean) input.isActionPressed("move_back")) dz += 1;
		if ((boolean) input.isActionPressed("move_forward")) dz -= 1;

		Vector3 direction = new Vector3(dx, 0, dz);
		if (dx != 0 || dz != 0) {
			double len = Math.sqrt(dx * dx + dz * dz);
			direction = new Vector3(dx / len, 0, dz / len);
			// Look at direction
			lookAt(new Vector3(
				(Vector3) getProperty("position") != null ? ((Vector3) getProperty("position")).getX() + direction.getX() : direction.getX(),
				(Vector3) getProperty("position") != null ? ((Vector3) getProperty("position")).getY() : 0,
				(Vector3) getProperty("position") != null ? ((Vector3) getProperty("position")).getZ() + direction.getZ() : direction.getZ()
			), new Vector3(0, 1, 0));
			if (animPlayer != null) animPlayer.setProperty("speed_scale", 4.0);
		} else {
			if (animPlayer != null) animPlayer.setProperty("speed_scale", 1.0);
		}

		Vector3 vel = (Vector3) getProperty("velocity");
		if (vel == null) vel = new Vector3(0, 0, 0);
		double vx = direction.getX() * speed;
		double vz = direction.getZ() * speed;
		double vy = vel.getY();

		boolean onFloor = (boolean) isOnFloor();
		if (onFloor && (boolean) (boolean) input.isActionJustPressed("jump")) {
			vy += jumpImpulse;
		}
		vy -= fallAcceleration * delta;

		setProperty("velocity", new Vector3(vx, vy, vz));
		moveAndSlide();

		// Check slide collisions for mob squashing
		long collisionCount = (long) call("get_slide_collision_count");
		for (int i = 0; i < collisionCount; i++) {
			org.godot.Godot collision = (org.godot.Godot) call("get_slide_collision", i);
			if (collision == null) continue;
			org.godot.Godot collider = (org.godot.Godot) collision.call("get_collider");
			if (collider == null) continue;

			boolean inMobGroup = (boolean) collider.call("is_in_group", "mob");
			if (inMobGroup) {
				Vector3 normal = (Vector3) collision.call("get_normal");
				// dot product with UP
				double dot = normal.getY(); // UP.y = 1
				if (dot > 0.1) {
					((STCMob) collider).squash();
					Vector3 curVel = (Vector3) getProperty("velocity");
					setProperty("velocity", new Vector3(curVel.getX(), bounceImpulse, curVel.getZ()));
					break;
				}
			}
		}

		// Arc rotation
		Vector3 curVel2 = (Vector3) getProperty("velocity");
		if (curVel2 != null) {
			double rotX = Math.PI / 6 * curVel2.getY() / jumpImpulse;
			setProperty("rotation", new Vector3(rotX, ((Vector3) getProperty("rotation")).getY(), ((Vector3) getProperty("rotation")).getZ()));
		}
	}

	@GodotMethod
	public void die() {
		emitSignal("hit");
		queueFree();
	}

	@GodotMethod
	public void OnMobDetectorBodyEntered(Object body) {
		die();
	}

	@Override
	public void _exitTree() {
		if (animPlayer != null) {
			animPlayer.stop();
			animPlayer = null;
		}
	}
}
