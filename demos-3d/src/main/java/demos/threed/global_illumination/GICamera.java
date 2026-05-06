package demos.threed.global_illumination;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Camera3D;
import org.godot.singleton.Input;

@GodotClass(name = "GICamera", parent = "Camera3D")
public class GICamera extends Camera3D {

	private static final double MOUSE_SENSITIVITY = 0.002;
	private static final double MOVE_SPEED = 1.5;

	private Vector3 rot = new Vector3(0, 0, 0);
	private Vector3 velocity = new Vector3(0, 0, 0);
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
		input.setMouseMode(2); // MOUSE_MODE_CAPTURED
	}

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		String className = ev.get_class_();

		if ("InputEventMouseMotion".equals(className)) {
			org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
			long mouseMode = (long) input.getMouseMode();
			if (mouseMode == 2) { // MOUSE_MODE_CAPTURED
				Vector2 rel = (org.godot.math.Vector2) ev.getProperty("screen_relative");
				if (rel != null) {
					double newY = rot.getY() - rel.getX() * MOUSE_SENSITIVITY;
					double newX = rot.getX() - rel.getY() * MOUSE_SENSITIVITY;
					newX = Math.max(-1.57, Math.min(1.57, newX));
					rot = new Vector3(newX, newY, 0);

					// Set transform basis from euler
					double sx = Math.sin(rot.getX());
					double cx = Math.cos(rot.getX());
					double sy = Math.sin(rot.getY());
					double cy = Math.cos(rot.getY());
					double sz = Math.sin(rot.getZ());
					double cz = Math.cos(rot.getZ());
					// Basis from Euler XYZ
					double[][] basis = new double[3][3];
					basis[0][0] = cy * cz + sy * sx * sz;
					basis[0][1] = -cy * sz + sy * sx * cz;
					basis[0][2] = sy * cx;
					basis[1][0] = cx * sz;
					basis[1][1] = cx * cz;
					basis[1][2] = -sx;
					basis[2][0] = -sy * cz + cy * sx * sz;
					basis[2][1] = sy * sz + cy * sx * cz;
					basis[2][2] = cy * cx;
					call("set_transform_basis_rows",
						new Vector3(basis[0][0], basis[0][1], basis[0][2]),
						new Vector3(basis[1][0], basis[1][1], basis[1][2]),
						new Vector3(basis[2][0], basis[2][1], basis[2][2]));
				}
			}
		}

		if ((boolean) ev.isActionPressed("toggle_mouse_capture")) {
			org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
			long mouseMode = (long) input.getMouseMode();
			input.setMouseMode(mouseMode == 2 ? 0 : 2);
		}
		return false;
	}

	@Override
	public void _process(double delta) {
		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();

		double mx = 0, mz = 0, my = 0;
		if ((boolean) input.isActionPressed("move_left")) mx -= 1;
		if ((boolean) input.isActionPressed("move_right")) mx += 1;
		if ((boolean) input.isActionPressed("move_forward")) mz -= 1;
		if ((boolean) input.isActionPressed("move_back")) mz += 1;
		if ((boolean) input.isActionPressed("move_down")) my -= 1;
		if ((boolean) input.isActionPressed("move_up")) my += 1;

		// Normalize horizontal motion
		double len = Math.sqrt(mx * mx + mz * mz);
		if (len > 0) { mx /= len; mz /= len; }

		// Get current basis for direction
		Vector3 pos = (Vector3) getProperty("position");
		if (pos == null) pos = new Vector3(0, 0, 0);

		// Simplified: just use velocity accumulation with damping
		double vx = velocity.getX() + MOVE_SPEED * delta * mx;
		double vy = velocity.getY() + MOVE_SPEED * delta * my;
		double vz = velocity.getZ() + MOVE_SPEED * delta * mz;
		vx *= 0.85; vy *= 0.85; vz *= 0.85;

		velocity = new Vector3(vx, vy, vz);
		setProperty("position", new Vector3(pos.getX() + vx, pos.getY() + vy, pos.getZ() + vz));
	}
}
