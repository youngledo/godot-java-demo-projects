package demos.threed.physics_interpolation;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;

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
		input.call("set_mouse_mode", 2);

		cycleCameraType();
	}

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.Godot ev = (org.godot.Godot) inputEvent;
		String cls = (String) ev.call("get_class");

		if ("InputEventMouseMotion".equals(cls)) {
			org.godot.math.Vector2 rel = (org.godot.math.Vector2) ev.getProperty("screen_relative");
			if (rel != null) {
				yaw -= rel.getX() * MOUSE_SENSITIVITY * 0.001;
				pitch += rel.getY() * MOUSE_SENSITIVITY * 0.002;
				pitch = Math.max(-Math.PI, Math.min(Math.PI, pitch));

				org.godot.Godot rig = (org.godot.Godot) call("get_node", "Rig");
				if (rig != null) rig.setProperty("rotation", new Vector3(0, yaw, 0));
			}
		}
		return false;
	}

	@Override
	public void _process(double delta) {
		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();

		if ((boolean) input.call("is_action_just_pressed", "cycle_camera_type")) {
			cycleCameraType();
		}

		if ((boolean) input.call("is_action_just_pressed", "jump")) {
			if ((boolean) call("is_on_floor")) {
				Vector3 vel = (Vector3) getProperty("velocity");
				if (vel == null) vel = new Vector3(0, 0, 0);
				setProperty("velocity", new Vector3(vel.getX(), JUMP_VELOCITY, vel.getZ()));
			}
		}

		if ((boolean) input.call("is_action_just_pressed", "reset_position")) {
			setProperty("position", new Vector3(0, 1, 0));
			setProperty("velocity", new Vector3(0, 0, 0));
			yaw = 0;
			pitch = 0;
		}

		// Update head rotation
		org.godot.Godot head = (org.godot.Godot) call("get_node", "Rig/Head");
		if (head != null) head.setProperty("rotation", new Vector3(pitch * -0.5, 0, 0));
	}

	@Override
	public void _physicsProcess(double delta) {
		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();

		double mx = 0, mz = 0;
		if ((boolean) input.call("is_action_pressed", "move_left")) mx -= 1;
		if ((boolean) input.call("is_action_pressed", "move_right")) mx += 1;
		if ((boolean) input.call("is_action_pressed", "move_forward")) mz -= 1;
		if ((boolean) input.call("is_action_pressed", "move_backward")) mz += 1;

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
		call("move_and_slide");
	}

	@GodotMethod
	public void cycleCameraType() {
		camType = (camType + 1) % 3;
		org.godot.Godot fpsCam = (org.godot.Godot) call("get_node", "Rig/Head/Camera_FPS");
		org.godot.Godot tpsCam = (org.godot.Godot) call("get_node", "Rig/Camera_TPS");

		if (camType == 1 && fpsCam != null) {
			fpsCam.call("make_current");
		} else if (camType == 2 && tpsCam != null) {
			tpsCam.call("make_current");
		} else {
			org.godot.Godot fixedCam = (org.godot.Godot) call("get_node", "../Camera_Fixed");
			if (fixedCam != null) fixedCam.call("make_current");
		}
	}
}
