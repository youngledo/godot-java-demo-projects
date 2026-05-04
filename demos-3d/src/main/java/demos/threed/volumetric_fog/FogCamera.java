package demos.threed.volumetric_fog;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector3;
import org.godot.node.Camera3D;
import org.godot.singleton.Input;

@GodotClass(name = "FogCamera", parent = "Camera3D")
public class FogCamera extends Camera3D {

	private static final double MOUSE_SENSITIVITY = 0.002;
	private static final double MOVE_SPEED = 0.6;

	private Vector3 rot = new Vector3(0, 0, 0);
	private Vector3 velocity = new Vector3(0, 0, 0);
	private org.godot.Godot label;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		label = (org.godot.Godot) call("get_node", "Label");
		Input input = Input.singleton();
		input.call("set_mouse_mode", 2); // MOUSE_MODE_CAPTURED
	}

	@Override
	public void _process(double delta) {
		Input input = Input.singleton();

		double mx = (double) input.call("get_action_strength", "move_right", false)
				- (double) input.call("get_action_strength", "move_left", false);
		double mz = (double) input.call("get_action_strength", "move_back", false)
				- (double) input.call("get_action_strength", "move_forward", false);

		double len = Math.sqrt(mx * mx + mz * mz);
		if (len > 0) { mx /= len; mz /= len; }

		Vector3 motion = new Vector3(mx, 0, mz);
		// Transform by basis (simplified - use raw motion for now)
		velocity = new Vector3(
			velocity.getX() + MOVE_SPEED * delta * motion.getX(),
			velocity.getY(),
			velocity.getZ() + MOVE_SPEED * delta * motion.getZ()
		);
		velocity = new Vector3(velocity.getX() * 0.85, velocity.getY() * 0.85, velocity.getZ() * 0.85);

		Vector3 pos = (Vector3) getProperty("position");
		if (pos != null) {
			setProperty("position", new Vector3(pos.getX() + velocity.getX(), pos.getY() + velocity.getY(), pos.getZ() + velocity.getZ()));
		}
	}

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.Godot ev = (org.godot.Godot) inputEvent;
		String className = (String) ev.call("get_class");

		if ("InputEventMouseMotion".equals(className)) {
			Object mouseMode = Input.singleton().call("get_mouse_mode");
			if (mouseMode != null && ((long) mouseMode) == 2) { // CAPTURED
				org.godot.math.Vector2 relative = (org.godot.math.Vector2) ev.getProperty("screen_relative");
				if (relative != null) {
					rot = new Vector3(
						clamp(rot.getX() - relative.getY() * MOUSE_SENSITIVITY, -1.57, 1.57),
						rot.getY() - relative.getX() * MOUSE_SENSITIVITY,
						rot.getZ()
					);
					call("set_rotation", rot);
				}
			}
		}

		if ((boolean) ev.call("is_action_pressed", "toggle_mouse_capture")) {
			Object mouseMode = Input.singleton().call("get_mouse_mode");
			if (mouseMode != null && ((long) mouseMode) == 2) {
				Input.singleton().call("set_mouse_mode", 0); // VISIBLE
			} else {
				Input.singleton().call("set_mouse_mode", 2); // CAPTURED
			}
		}
		return false;
	}

	private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
}
