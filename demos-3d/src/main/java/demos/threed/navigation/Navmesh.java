package demos.threed.navigation;

import org.godot.annotation.GodotClass;
import org.godot.collection.GodotDictionary;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.PhysicsDirectSpaceState3D;
import org.godot.node.PhysicsRayQueryParameters3D;
import org.godot.node.World3D;

@GodotClass(name = "Navmesh", parent = "Node3D")
public class Navmesh extends Node3D {

	private double camRotation = 0.0;
	private org.godot.node.Camera3D camera;
	private NavRobot robot;
	private Node3D cameraBase;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		camera = getNodeAs("CameraBase/Camera3D", org.godot.node.Camera3D.class);
		robot = getNodeAs("RobotBase", NavRobot.class);
		cameraBase = getNodeAs("CameraBase", Node3D.class);
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		String className = ev.get_class_();

		if ("InputEventMouseButton".equals(className)) {
			long buttonIndex = (long) ev.getProperty("button_index");
			boolean pressed = (boolean) ev.getProperty("pressed");

			if (buttonIndex == 1 && pressed) {
				if (camera == null || robot == null) return false;

				Vector2 mousePos = (Vector2) ev.getProperty("position");
				Vector3 rayOrigin = camera.projectRayOrigin(mousePos);
				Vector3 rayDir = camera.projectRayNormal(mousePos);
				if (rayOrigin == null || rayDir == null) return false;

				double rayLength = 1000.0;
				Vector3 rayEnd = new Vector3(
					rayOrigin.getX() + rayDir.getX() * rayLength,
					rayOrigin.getY() + rayDir.getY() * rayLength,
					rayOrigin.getZ() + rayDir.getZ() * rayLength
				);

				World3D world3d = getWorld3d();
				if (world3d == null) return false;

				PhysicsDirectSpaceState3D spaceState = world3d.getDirectSpaceState();
				if (spaceState != null) {
					PhysicsRayQueryParameters3D query = PhysicsRayQueryParameters3D.create(rayOrigin, rayEnd);
					GodotDictionary result = spaceState.intersectRay(query);
					Object hitPos = result != null ? result.get("position") : null;
					if (hitPos instanceof Vector3 targetPosition) {
						robot.setTargetPosition(targetPosition);
					}
				}
				return true;
			}
		}

		if ("InputEventMouseMotion".equals(className)) {
			long buttonMask = (long) ev.getProperty("button_mask");
			if ((buttonMask & 4) != 0 || (buttonMask & 2) != 0) {
				Vector2 relative = (Vector2) ev.getProperty("screen_relative");
				if (relative != null) {
					camRotation -= relative.getX() * 0.005;
					if (cameraBase != null) {
						cameraBase.setRotation(new Vector3(0, camRotation, 0));
					}
				}
				return true;
			}
		}
		return false;
	}
}
