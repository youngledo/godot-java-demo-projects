package demos.threed.soft_body_physics;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.WorldEnvironment;

@GodotClass(name = "SoftBodyTester", parent = "WorldEnvironment")
public class SoftBodyTester extends WorldEnvironment {

	private static final double ROT_SPEED = 0.003;
	private static final double ZOOM_SPEED = 0.125;
	private static final int MAIN_BUTTONS = 1 | 2 | 4;

	private int testerIndex = 0;
	private double rotX = Math.toRadians(-22.5);
	private double rotY = Math.toRadians(90);
	private double zoom = 1.5;

	private org.godot.Godot testers;
	private org.godot.Godot cameraHolder;
	private org.godot.Godot rotationX;
	private org.godot.Godot camera;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		testers = (org.godot.Godot) call("get_node", "Testers");
		cameraHolder = (org.godot.Godot) call("get_node", "CameraHolder");
		rotationX = (org.godot.Godot) call("get_node", "CameraHolder/RotationX");
		camera = (org.godot.Godot) call("get_node", "CameraHolder/RotationX/Camera3D");

		if (cameraHolder != null) cameraHolder.call("set_rotation", new Vector3(0, rotY, 0));
		if (rotationX != null) rotationX.call("set_rotation", new Vector3(rotX, 0, 0));
		updateGui();
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.Godot ev = (org.godot.Godot) inputEvent;
		String className = (String) ev.call("get_class");

		if ((boolean) ev.call("is_action_pressed", "ui_left")) { onPreviousPressed(); return true; }
		if ((boolean) ev.call("is_action_pressed", "ui_right")) { onNextPressed(); return true; }

		if ((boolean) ev.call("is_action_pressed", "reset_physics_simulation")) {
			// Simplified: just reload the scene
			org.godot.Godot tree = (org.godot.Godot) call("get_tree");
			if (tree != null) tree.call("reload_current_scene");
			return true;
		}

		if ("InputEventMouseButton".equals(className)) {
			long buttonIndex = (long) ev.getProperty("button_index");
			if (buttonIndex == 4) zoom -= ZOOM_SPEED;
			else if (buttonIndex == 5) zoom += ZOOM_SPEED;
			zoom = clamp(zoom, 1.5, 4);
			return true;
		}

		if ("InputEventMouseMotion".equals(className)) {
			long buttonMask = (long) ev.getProperty("button_mask");
			if ((buttonMask & MAIN_BUTTONS) != 0) {
				Vector2 relative = (Vector2) ev.getProperty("screen_relative");
				rotY -= relative.getX() * ROT_SPEED;
				rotX -= relative.getY() * ROT_SPEED;
				rotX = clamp(rotX, Math.toRadians(-90), 0);
				if (cameraHolder != null) cameraHolder.call("set_rotation", new Vector3(0, rotY, 0));
				if (rotationX != null) rotationX.call("set_rotation", new Vector3(rotX, 0, 0));
				return true;
			}
		}
		return false;
	}

	@Override
	public void _process(double delta) {
		if (testers == null || cameraHolder == null || camera == null) return;
		org.godot.Godot currentTester = (org.godot.Godot) testers.call("get_child", testerIndex);
		if (currentTester == null) return;

		Vector3 holderPos = (Vector3) cameraHolder.getProperty("global_position");
		Vector3 testerPos = (Vector3) currentTester.getProperty("global_position");
		double newZ = lerp(holderPos.getZ(), testerPos.getZ(), 3 * delta);
		cameraHolder.setProperty("global_position", new Vector3(holderPos.getX(), holderPos.getY(), newZ));

		Vector3 camPos = (Vector3) camera.getProperty("position");
		double newCamZ = lerp(camPos.getZ(), zoom, 10 * delta);
		camera.setProperty("position", new Vector3(camPos.getX(), camPos.getY(), newCamZ));
	}

	@GodotMethod
	public void onPreviousPressed() { testerIndex = Math.max(0, testerIndex - 1); updateGui(); }
	@GodotMethod
	public void onNextPressed() {
		if (testers != null) {
			int count = (int) (long) testers.call("get_child_count");
			testerIndex = Math.min(testerIndex + 1, count - 1);
		}
		updateGui();
	}

	private void updateGui() {
		if (testers == null) return;
		org.godot.Godot currentTester = (org.godot.Godot) testers.call("get_child", testerIndex);
		if (currentTester == null) return;
		String name = (String) currentTester.call("get_name");

		org.godot.Godot testName = (org.godot.Godot) call("get_node", "TestName");
		if (testName != null) testName.setProperty("text", capitalize(name));
		org.godot.Godot prevBtn = (org.godot.Godot) call("get_node", "Previous");
		org.godot.Godot nextBtn = (org.godot.Godot) call("get_node", "Next");
		if (prevBtn != null) prevBtn.setProperty("disabled", testerIndex == 0);
		if (nextBtn != null) {
			int count = (int) (long) testers.call("get_child_count");
			nextBtn.setProperty("disabled", testerIndex == count - 1);
		}
	}

	private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
	private static double lerp(double from, double to, double w) { return from + (to - from) * w; }
	private static String capitalize(String s) {
		String[] parts = s.split("_");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) sb.append(" ");
			if (!parts[i].isEmpty()) {
				sb.append(Character.toUpperCase(parts[i].charAt(0)));
				if (parts[i].length() > 1) sb.append(parts[i].substring(1));
			}
		}
		return sb.toString();
	}
}
