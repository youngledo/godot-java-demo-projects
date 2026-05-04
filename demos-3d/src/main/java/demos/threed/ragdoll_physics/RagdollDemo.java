package demos.threed.ragdoll_physics;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Node3D;

@GodotClass(name = "RagdollDemo", parent = "Node3D")
public class RagdollDemo extends Node3D {

	private static final double MOUSE_SENSITIVITY = 0.01;
	private static final double INITIAL_VELOCITY_STRENGTH = 0.5;

	private org.godot.Godot cameraPivot;
	private org.godot.Godot camera;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		cameraPivot = (org.godot.Godot) call("get_node", "CameraPivot");
		camera = (org.godot.Godot) call("get_node", "CameraPivot/Camera3D");
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.Godot ev = (org.godot.Godot) inputEvent;
		String className = (String) ev.call("get_class");

		if ((boolean) ev.call("is_action_pressed", "reset_simulation")) {
			org.godot.Godot tree = (org.godot.Godot) call("get_tree");
			if (tree != null) tree.call("reload_current_scene");
			return true;
		}

		if ((boolean) ev.call("is_action_pressed", "place_ragdoll")) {
			placeRagdoll();
			return true;
		}

		if ((boolean) ev.call("is_action_pressed", "slow_motion")) {
			org.godot.Godot engine = (org.godot.Godot) call("get_node", "/root/Engine");
			if (engine != null) engine.setProperty("time_scale", 0.25);
			return true;
		}

		if ((boolean) ev.call("is_action_released", "slow_motion")) {
			org.godot.Godot engine = (org.godot.Godot) call("get_node", "/root/Engine");
			if (engine != null) engine.setProperty("time_scale", 1.0);
			return true;
		}

		if ("InputEventMouseMotion".equals(className)) {
			long buttonMask = (long) ev.getProperty("button_mask");
			if ((buttonMask & 2) != 0) { // RIGHT button
				Vector2 relative = (Vector2) ev.getProperty("screen_relative");
				if (relative != null && cameraPivot != null) {
					Vector3 rot = (Vector3) cameraPivot.getProperty("global_rotation");
					if (rot != null) {
						double newX = clamp(rot.getX() - relative.getY() * MOUSE_SENSITIVITY, -Math.PI * 2 * 0.249, Math.PI * 2 * 0.021);
						double newY = rot.getY() - relative.getX() * MOUSE_SENSITIVITY;
						cameraPivot.setProperty("global_rotation", new Vector3(newX, newY, rot.getZ()));
					}
				}
				return true;
			}
		}

		if ("InputEventMouseButton".equals(className)) {
			long buttonIndex = (long) ev.getProperty("button_index");
			if (buttonIndex == 4 && camera != null) { // WHEEL_UP
				camera.call("translate_object_local", new Vector3(0, 0, -0.5));
				return true;
			} else if (buttonIndex == 5 && camera != null) { // WHEEL_DOWN
				camera.call("translate_object_local", new Vector3(0, 0, 0.5));
				return true;
			}
		}
		return false;
	}

	private void placeRagdoll() {
		if (camera == null) return;

		Vector3 origin = (Vector3) camera.getProperty("global_position");
		org.godot.Godot viewport = (org.godot.Godot) call("get_viewport");
		Vector2 mousePos = viewport != null ? (Vector2) viewport.call("get_mouse_position") : null;
		Vector3 target = mousePos != null ? (Vector3) camera.call("project_position", mousePos, 100) : null;

		if (origin == null || target == null) return;

		org.godot.Godot world3d = (org.godot.Godot) camera.call("get_world_3d");
		if (world3d == null) return;
		org.godot.Godot spaceState = (org.godot.Godot) world3d.call("get_direct_space_state");
		if (spaceState == null) return;

		org.godot.Godot query = (org.godot.Godot) spaceState.call("create_ray_query", origin, target);
		if (query == null) return;

		Object result = spaceState.call("intersect_ray", query);
		if (result == null) return;

		org.godot.Godot resDict = (org.godot.Godot) result;
		Object hitPos = resDict.call("get", "position");
		if (hitPos == null) return;

		Object ragdollScene = call("load", "res://characters/mannequiny_ragdoll.tscn");
		if (ragdollScene == null) return;

		org.godot.Godot ragdoll = (org.godot.Godot) ((org.godot.Godot) ragdollScene).call("instantiate");
		if (ragdoll == null) return;

		Vector3 hitVector = (Vector3) hitPos;
		ragdoll.setProperty("position", new Vector3(hitVector.getX(), hitVector.getY() + 0.5, hitVector.getZ()));

		if (cameraPivot != null) {
			Vector3 pivotRot = (Vector3) cameraPivot.getProperty("rotation");
			if (pivotRot != null) ragdoll.setProperty("rotation", new Vector3(0, pivotRot.getY(), 0));
		}

		// Random initial velocity
		double angle = Math.random() * Math.PI * 2;
		Vector3 initVel = new Vector3(
			-Math.sin(angle) * INITIAL_VELOCITY_STRENGTH,
			0,
			-Math.cos(angle) * INITIAL_VELOCITY_STRENGTH
		);
		ragdoll.setProperty("initial_velocity", initVel);

		call("add_child", ragdoll);
	}

	private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
}
