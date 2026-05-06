package demos.threed.rigidbody_character;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.RigidBody3D;
import org.godot.singleton.Input;
import org.godot.node.Node;
import org.godot.node.SceneTree;

@GodotClass(name = "RBCCubio", parent = "RigidBody3D")
public class RBCCubio extends RigidBody3D {

	private org.godot.node.Node shapeCast;
	private org.godot.node.Camera3D camera;
	private org.godot.node.Label winText;
	private Vector3 startPosition;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		shapeCast = getNode("ShapeCast3D");
		camera = (org.godot.node.Camera3D) getNode("Target/Camera3D");
		winText = (org.godot.node.Label) getNode("WinText");
		startPosition = (Vector3) getProperty("position");
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
			setProperty("linear_velocity", new Vector3(0, 0, 0));
			resetPhysicsInterpolation();
		}

		double dx = (double) input.call("get_axis", "move_left", "move_right");
		double dz = (double) input.call("get_axis", "move_forward", "move_back");
		Vector3 dir = new Vector3(dx, 0, dz);
		double len = Math.sqrt(dx * dx + dz * dz);
		if (len > 1) {
			dir = new Vector3(dx / len, 0, dz / len);
		}

		// Air movement
		Vector3 impulse = new Vector3(dir.getX() * 5.0 * delta, 0, dir.getZ() * 5.0 * delta);
		applyCentralImpulse(impulse);

		if (onGround()) {
			// Ground movement (higher acceleration)
			Vector3 groundImpulse = new Vector3(dir.getX() * 10.0 * delta, 0, dir.getZ() * 10.0 * delta);
			applyCentralImpulse(groundImpulse);

			if ((boolean) input.isActionPressed("jump")) {
				setProperty("linear_velocity", new Vector3(
					((Vector3) getProperty("linear_velocity")).getX(),
					7.0,
					((Vector3) getProperty("linear_velocity")).getZ()
				));
			}
		}
	}

	private boolean onGround() {
		return shapeCast != null && (boolean) shapeCast.call("is_colliding");
	}

	@GodotMethod
	public void OnTcubeBodyEntered(Object body) {
		if (body == this && winText != null) {
			winText.setProperty("visible", true);
		}
	}
}
