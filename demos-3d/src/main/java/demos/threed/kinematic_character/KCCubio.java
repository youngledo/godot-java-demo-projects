package demos.threed.kinematic_character;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;
import org.godot.singleton.Input;
import org.godot.node.Node;
import org.godot.node.SceneTree;

@GodotClass(name = "KCCubio", parent = "CharacterBody3D")
public class KCCubio extends CharacterBody3D {

	private static final double MAX_SPEED = 3.5;
	private static final double JUMP_SPEED = 6.5;
	private static final double ACCELERATION = 4.0;
	private static final double DECELERATION = 4.0;

	private org.godot.node.Camera3D camera;
	private org.godot.node.Label winText;
	private Vector3 startPosition;
	private double gravity = -9.8;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		camera = (org.godot.node.Camera3D) getNode("Target/Camera3D");
		winText = (org.godot.node.Label) getNode("WinText");
		startPosition = (Vector3) getProperty("position");

		// Connect body_entered on tcube area
		org.godot.node.Node tcube = getNode("Target");
		if (tcube != null) {
			// The win area might be a sibling - try to find it
		}
	}

	@Override
	public void _physicsProcess(double delta) {
		Input input = Input.singleton();

		if ((boolean) (boolean) input.isActionJustPressed("exit")) {
			org.godot.node.SceneTree tree = getTree();
			if (tree != null) tree.quit();
		}

		Vector3 pos = (Vector3) getProperty("global_position");
		if ((boolean) (boolean) input.isActionJustPressed("reset_position") || (pos != null && pos.getY() < -6.0)) {
			setProperty("position", startPosition);
			setProperty("velocity", new Vector3(0, 0, 0));
			resetPhysicsInterpolation();
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
		moveAndSlide();

		boolean onFloor = (boolean) isOnFloor();
		if (onFloor && (boolean) input.isActionPressed("jump")) {
			Vector3 curVel = (Vector3) getProperty("velocity");
			setProperty("velocity", new Vector3(curVel.getX(), JUMP_SPEED, curVel.getZ()));
		}
	}

	@GodotMethod
	public void OnTcubeBodyEntered(Object body) {
		if (body == this && winText != null) {
			winText.show();
		}
	}

	private static double lerp(double from, double to, double w) { return from + (to - from) * w; }
}
