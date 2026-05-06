package demos.threed.navigation;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "Navmesh", parent = "Node3D")
public class Navmesh extends Node3D {

	private double camRotation = 0.0;
	private org.godot.node.Camera3D camera;
	private org.godot.node.Node robot;
	private org.godot.node.Camera3D cameraBase;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		camera = (org.godot.node.Camera3D) getNode("CameraBase/Camera3D");
		robot = getNode("RobotBase");
		cameraBase = (org.godot.node.Camera3D) getNode("CameraBase");
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		String className = ev.get_class_();

		if ("InputEventMouseButton".equals(className)) {
			long buttonIndex = (long) ev.getProperty("button_index");
			boolean pressed = (boolean) ev.getProperty("pressed");

			if (buttonIndex == 1 && pressed) { // LEFT click
				if (camera == null || robot == null) return false;

				Vector2 mousePos = (Vector2) ev.getProperty("position");
				Vector3 rayOrigin = (Vector3) camera.projectRayOrigin(mousePos);
				Vector3 rayDir = (Vector3) camera.projectRayNormal(mousePos);
				if (rayOrigin == null || rayDir == null) return false;

				// Calculate ray end
				double rayLength = 1000.0;
				Vector3 rayEnd = new Vector3(
					rayOrigin.getX() + rayDir.getX() * rayLength,
					rayOrigin.getY() + rayDir.getY() * rayLength,
					rayOrigin.getZ() + rayDir.getZ() * rayLength
				);

				// Get closest point on navmesh
				org.godot.Godot world3d = (org.godot.Godot) call("get_world_3d");
				if (world3d == null) return false;
				Object navMap = world3d.getProperty("navigation_map");
				org.godot.node.Node navServer = getNode("/root/NavigationServer3D");

				// Use NavigationServer3D to find closest point
				// Simplified: use ray cast result directly
				org.godot.Godot spaceState = (org.godot.Godot) world3d.call("get_direct_space_state");
				if (spaceState != null) {
					org.godot.Godot query = (org.godot.Godot) spaceState.call("create_ray_query", rayOrigin, rayEnd);
					if (query != null) {
						Object result = spaceState.call("intersect_ray", query);
						if (result != null) {
							org.godot.Godot resDict = (org.godot.Godot) result;
							Object hitPos = resDict.call("get", "position");
							if (hitPos != null && robot != null) {
								robot.call("set_target_position", hitPos);
							}
						}
					}
				}
				return true;
			}
		}

		if ("InputEventMouseMotion".equals(className)) {
			long buttonMask = (long) ev.getProperty("button_mask");
			// Middle or right button drag
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
