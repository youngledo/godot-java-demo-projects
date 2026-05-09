package demos.threed.ragdoll_physics;

import org.godot.annotation.GodotClass;
import org.godot.collection.GodotDictionary;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.PackedScene;
import org.godot.node.PhysicsDirectSpaceState3D;
import org.godot.node.PhysicsRayQueryParameters3D;
import org.godot.node.World3D;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "RagdollDemo", parent = "Node3D")
public class RagdollDemo extends Node3D {

	private static final double MOUSE_SENSITIVITY = 0.01;
	private static final double INITIAL_VELOCITY_STRENGTH = 0.5;

	private Node3D cameraPivot;
	private org.godot.node.Camera3D camera;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		cameraPivot = getNodeAs("CameraPivot", Node3D.class);
		camera = getNodeAs("CameraPivot/Camera3D", org.godot.node.Camera3D.class);
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		String className = ev.get_class_();

		if (ev.isActionPressed("reset_simulation")) {
			org.godot.node.SceneTree tree = getTree();
			if (tree != null) tree.reloadCurrentScene();
			return true;
		}

		if (ev.isActionPressed("place_ragdoll")) {
			placeRagdoll();
			return true;
		}

		if (ev.isActionPressed("slow_motion")) {
			org.godot.node.Node engine = getNode("/root/Engine");
			if (engine != null) engine.setProperty("time_scale", 0.25);
			return true;
		}

		if (ev.isActionReleased("slow_motion")) {
			org.godot.node.Node engine = getNode("/root/Engine");
			if (engine != null) engine.setProperty("time_scale", 1.0);
			return true;
		}

		if ("InputEventMouseMotion".equals(className)) {
			long buttonMask = (long) ev.getProperty("button_mask");
			if ((buttonMask & 2) != 0) {
				Vector2 relative = (Vector2) ev.getProperty("screen_relative");
				if (relative != null && cameraPivot != null) {
					Vector3 rot = cameraPivot.getGlobalRotation();
					double newX = clamp(rot.getX() - relative.getY() * MOUSE_SENSITIVITY, -Math.PI * 2 * 0.249, Math.PI * 2 * 0.021);
					double newY = rot.getY() - relative.getX() * MOUSE_SENSITIVITY;
					cameraPivot.setGlobalRotation(new Vector3(newX, newY, rot.getZ()));
				}
				return true;
			}
		}

		if ("InputEventMouseButton".equals(className)) {
			long buttonIndex = (long) ev.getProperty("button_index");
			if (buttonIndex == 4 && camera != null) {
				camera.translateObjectLocal(new Vector3(0, 0, -0.5));
				return true;
			} else if (buttonIndex == 5 && camera != null) {
				camera.translateObjectLocal(new Vector3(0, 0, 0.5));
				return true;
			}
		}
		return false;
	}

	private void placeRagdoll() {
		if (camera == null) return;

		Vector3 origin = camera.getGlobalPosition();
		org.godot.node.Viewport viewport = getViewport();
		Vector2 mousePos = viewport != null ? viewport.getMousePosition() : null;
		Vector3 target = mousePos != null ? camera.projectPosition(mousePos, 100) : null;
		if (target == null) return;

		World3D world3d = camera.getWorld3d();
		if (world3d == null) return;
		PhysicsDirectSpaceState3D spaceState = world3d.getDirectSpaceState();
		if (spaceState == null) return;

		PhysicsRayQueryParameters3D query = PhysicsRayQueryParameters3D.create(origin, target);
		GodotDictionary result = spaceState.intersectRay(query);
		Object hitPos = result != null ? result.get("position") : null;
		if (!(hitPos instanceof Vector3 hitVector)) return;

		if (!(ResourceLoader.singleton().load("res://characters/mannequiny_ragdoll.tscn") instanceof PackedScene ragdollScene)) return;
		if (!(ragdollScene.instantiate() instanceof Node3D ragdoll)) return;

		ragdoll.setPosition(new Vector3(hitVector.getX(), hitVector.getY() + 0.5, hitVector.getZ()));

		if (cameraPivot != null) {
			Vector3 pivotRot = cameraPivot.getRotation();
			ragdoll.setRotation(new Vector3(0, pivotRot.getY(), 0));
		}

		double angle = Math.random() * Math.PI * 2;
		Vector3 initVel = new Vector3(
			-Math.sin(angle) * INITIAL_VELOCITY_STRENGTH,
			0,
			-Math.cos(angle) * INITIAL_VELOCITY_STRENGTH
		);
		ragdoll.setProperty("initial_velocity", initVel);

		addChild(ragdoll);
	}

	private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
}
