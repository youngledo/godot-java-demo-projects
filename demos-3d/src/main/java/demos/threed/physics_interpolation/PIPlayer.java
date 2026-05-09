package demos.threed.physics_interpolation;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;
import org.godot.node.Node;
import org.godot.singleton.Input;

@GodotClass(name = "PIPlayer", parent = "CharacterBody3D")
public class PIPlayer extends CharacterBody3D {

	private static final double MOUSE_SENSITIVITY = 2.5;
	private static final double MOVE_SPEED = 3.0;
	private static final double FRICTION = 10.0;
	private static final double JUMP_VELOCITY = 8.0;

	private double yaw = 0.0;
	private double pitch = 0.0;
	private int camType = 0; // 0=FIXED, 1=FPS, 2=TPS
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
		input.setMouseMode(2);

		cycleCameraType();
	}

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		String cls = ev.get_class_();

		if ("InputEventMouseMotion".equals(cls)) {
			org.godot.math.Vector2 rel = (org.godot.math.Vector2) ev.getProperty("screen_relative");
			if (rel != null) {
				yaw -= rel.getX() * MOUSE_SENSITIVITY * 0.001;
				pitch += rel.getY() * MOUSE_SENSITIVITY * 0.002;
				pitch = Math.max(-Math.PI, Math.min(Math.PI, pitch));

				org.godot.node.Node rig = getNode("Rig");
				if (rig != null) rig.setProperty("rotation", new Vector3(0, yaw, 0));
			}
		}
		return false;
	}

	@Override
	public void _process(double delta) {
		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();

		if ((boolean) (boolean) input.isActionJustPressed("cycle_camera_type")) {
			cycleCameraType();
		}

		if ((boolean) (boolean) input.isActionJustPressed("jump")) {
			if (((boolean) isOnFloor())) {
				Vector3 vel = (Vector3) getProperty("velocity");
				if (vel == null) vel = new Vector3(0, 0, 0);
				setProperty("velocity", new Vector3(vel.getX(), JUMP_VELOCITY, vel.getZ()));
			}
		}

		if ((boolean) (boolean) input.isActionJustPressed("reset_position")) {
			setProperty("position", new Vector3(0, 1, 0));
			setProperty("velocity", new Vector3(0, 0, 0));
			yaw = 0;
			pitch = 0;
		}

		// Update head rotation
		org.godot.node.Node head = getNode("Rig/Head");
		if (head != null) head.setProperty("rotation", new Vector3(pitch * -0.5, 0, 0));
	}

	@Override
	public void _physicsProcess(double delta) {
		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();

		double mx = 0, mz = 0;
		if ((boolean) input.isActionPressed("move_left")) mx -= 1;
		if ((boolean) input.isActionPressed("move_right")) mx += 1;
		if ((boolean) input.isActionPressed("move_forward")) mz -= 1;
		if ((boolean) input.isActionPressed("move_backward")) mz += 1;

		// Rotate input by yaw
		double cosY = Math.cos(yaw);
		double sinY = Math.sin(yaw);
		double rmx = mx * cosY - mz * sinY;
		double rmz = mx * sinY + mz * cosY;

		Vector3 vel = (Vector3) getProperty("velocity");
		if (vel == null) vel = new Vector3(0, 0, 0);

		double vx = vel.getX() + rmx * MOVE_SPEED;
		double vz = vel.getZ() + rmz * MOVE_SPEED;
		double vy = vel.getY() - 9.8 * delta;

		double frictionDelta = Math.exp(-FRICTION * delta);
		vx *= frictionDelta;
		vz *= frictionDelta;

		setProperty("velocity", new Vector3(vx, vy, vz));
		moveAndSlide();
	}

	@GodotMethod
	public void cycleCameraType() {
		camType = (camType + 1) % 3;
		org.godot.node.Camera3D fpsCam = getNodeAs("Rig/Head/Camera_FPS", org.godot.node.Camera3D.class);
		org.godot.node.Camera3D tpsCam = getNodeAs("Rig/Camera_TPS", org.godot.node.Camera3D.class);

		if (camType == 1 && fpsCam != null) {
			fpsCam.makeCurrent();
		} else if (camType == 2 && tpsCam != null) {
			tpsCam.makeCurrent();
		} else {
			org.godot.node.Camera3D fixedCam = getNodeAs("../Camera_Fixed", org.godot.node.Camera3D.class);
			if (fixedCam != null) fixedCam.makeCurrent();
		}
	}
}
