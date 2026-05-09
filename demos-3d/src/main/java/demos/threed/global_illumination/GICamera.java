package demos.threed.global_illumination;

import org.godot.annotation.GodotClass;
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

		Input input = Input.singleton();
		input.setMouseMode(2);
	}

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		String className = ev.get_class_();

		if ("InputEventMouseMotion".equals(className)) {
			Input input = Input.singleton();
			long mouseMode = input.getMouseMode();
			if (mouseMode == 2) {
				Vector2 rel = (Vector2) ev.getProperty("screen_relative");
				if (rel != null) {
					double newY = rot.getY() - rel.getX() * MOUSE_SENSITIVITY;
					double newX = rot.getX() - rel.getY() * MOUSE_SENSITIVITY;
					newX = Math.max(-1.57, Math.min(1.57, newX));
					rot = new Vector3(newX, newY, 0);
					setRotation(rot);
				}
			}
		}

		if (ev.isActionPressed("toggle_mouse_capture")) {
			Input input = Input.singleton();
			long mouseMode = input.getMouseMode();
			input.setMouseMode(mouseMode == 2 ? 0 : 2);
		}
		return false;
	}

	@Override
	public void _process(double delta) {
		Input input = Input.singleton();

		double mx = 0, mz = 0, my = 0;
		if (input.isActionPressed("move_left")) mx -= 1;
		if (input.isActionPressed("move_right")) mx += 1;
		if (input.isActionPressed("move_forward")) mz -= 1;
		if (input.isActionPressed("move_back")) mz += 1;
		if (input.isActionPressed("move_down")) my -= 1;
		if (input.isActionPressed("move_up")) my += 1;

		double len = Math.sqrt(mx * mx + mz * mz);
		if (len > 0) { mx /= len; mz /= len; }

		Vector3 pos = getPosition();
		if (pos == null) pos = new Vector3(0, 0, 0);

		double vx = velocity.getX() + MOVE_SPEED * delta * mx;
		double vy = velocity.getY() + MOVE_SPEED * delta * my;
		double vz = velocity.getZ() + MOVE_SPEED * delta * mz;
		vx *= 0.85; vy *= 0.85; vz *= 0.85;

		velocity = new Vector3(vx, vy, vz);
		setPosition(new Vector3(pos.getX() + vx, pos.getY() + vy, pos.getZ() + vz));
	}
}
