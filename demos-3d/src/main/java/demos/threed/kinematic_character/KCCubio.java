package demos.threed.kinematic_character;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;
import org.godot.singleton.Input;

@GodotClass(name = "KCCubio", parent = "CharacterBody3D")
public class KCCubio extends CharacterBody3D {

	private static final double MAX_SPEED = 3.5;
	private static final double JUMP_SPEED = 6.5;
	private static final double ACCELERATION = 4.0;
	private static final double DECELERATION = 4.0;

	private org.godot.Godot camera;
	private org.godot.Godot winText;
	private Vector3 startPosition;
	private double gravity = -9.8;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		camera = (org.godot.Godot) call("get_node", "Target/Camera3D");
		winText = (org.godot.Godot) call("get_node", "WinText");
		startPosition = (Vector3) getProperty("position");

		// Connect body_entered on tcube area
		org.godot.Godot tcube = (org.godot.Godot) call("get_node", "Target");
		if (tcube != null) {
			// The win area might be a sibling - try to find it
		}
	}

	@Override
	public void _physicsProcess(double delta) {
		Input input = Input.singleton();

		if ((boolean) input.call("is_action_just_pressed", "exit", false)) {
			org.godot.Godot tree = (org.godot.Godot) call("get_tree");
			if (tree != null) tree.call("quit");
		}

		Vector3 pos = (Vector3) getProperty("global_position");
		if ((boolean) input.call("is_action_just_pressed", "reset_position", false) || (pos != null && pos.getY() < -6.0)) {
			setProperty("position", startPosition);
			setProperty("velocity", new Vector3(0, 0, 0));
			call("reset_physics_interpolation");
		}

		double dx = (double) input.call("get_axis", "move_left", "move_right");
		double dz = (double) input.call("get_axis", "move_forward", "move_back");
		Vector3 dir = new Vector3(dx, 0, dz);

		// Get camera basis and transform direction
		if (camera != null) {
			Object camBasisObj = camera.getProperty("global_transform");
			if (camBasisObj != null) {
				// Use camera's basis to transform the direction
				Object camBasis = ((org.godot.Godot) camBasisObj).getProperty("basis");
				// Simplified: just use raw direction
				dir = new Vector3(dx, 0, dz);
			}
		}

		double dirLen = Math.sqrt(dir.getX() * dir.getX() + dir.getZ() * dir.getZ());
		if (dirLen > 1) {
			dir = new Vector3(dir.getX() / dirLen, 0, dir.getZ() / dirLen);
		}

		Vector3 vel = (Vector3) getProperty("velocity");
		if (vel == null) vel = new Vector3(0, 0, 0);
		double vx = vel.getX();
		double vy = vel.getY();
		double vz = vel.getZ();

		vy += delta * gravity;

		// Horizontal velocity interpolation
		double hvx = vx, hvz = vz;
		double tx = dir.getX() * MAX_SPEED;
		double tz = dir.getZ() * MAX_SPEED;

		double accel = (dir.getX() * hvx + dir.getZ() * hvz) > 0 ? ACCELERATION : DECELERATION;
		hvx = lerp(hvx, tx, accel * delta);
		hvz = lerp(hvz, tz, accel * delta);

		setProperty("velocity", new Vector3(hvx, vy, hvz));
		call("move_and_slide");

		boolean onFloor = (boolean) call("is_on_floor");
		if (onFloor && (boolean) input.call("is_action_pressed", "jump", false)) {
			Vector3 curVel = (Vector3) getProperty("velocity");
			setProperty("velocity", new Vector3(curVel.getX(), JUMP_SPEED, curVel.getZ()));
		}
	}

	@GodotMethod
	public void _on_tcube_body_entered(Object body) {
		if (body == this && winText != null) {
			winText.call("show");
		}
	}

	private static double lerp(double from, double to, double w) { return from + (to - from) * w; }
}
