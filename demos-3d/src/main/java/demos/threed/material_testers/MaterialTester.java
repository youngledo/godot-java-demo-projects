package demos.threed.material_testers;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Node3D;

@GodotClass(name = "MaterialTester", parent = "Node3D")
public class MaterialTester extends Node3D {

	private static final double INTERP_SPEED = 2.0;
	private static final double ROT_SPEED = 0.003;
	private static final double ZOOM_SPEED = 0.1;
	private static final int MAIN_BUTTONS = 1 | 2 | 4;

	private int testerIndex = 0;
	private double rotX = -0.5;
	private double rotY = -0.5;
	private double zoom = 5.0;

	private org.godot.Godot testers;
	private org.godot.Godot cameraHolder;
	private org.godot.Godot rotationX;
	private org.godot.Godot camera;
	private org.godot.Godot materialName;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		testers = (org.godot.Godot) call("get_node", "Testers");
		cameraHolder = (org.godot.Godot) call("get_node", "CameraHolder");
		rotationX = (org.godot.Godot) call("get_node", "CameraHolder/RotationX");
		camera = (org.godot.Godot) call("get_node", "CameraHolder/RotationX/Camera");
		materialName = (org.godot.Godot) call("get_node", "UI/MaterialName");

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

		if ("InputEventMouseButton".equals(className)) {
			long buttonIndex = (long) ev.getProperty("button_index");
			if (buttonIndex == 4) zoom -= ZOOM_SPEED;
			else if (buttonIndex == 5) zoom += ZOOM_SPEED;
			zoom = clamp(zoom, 2, 8);
			if (camera != null) camera.setProperty("position", new Vector3(0, 0, zoom));
			return true;
		}

		if ("InputEventMouseMotion".equals(className)) {
			long buttonMask = (long) ev.getProperty("button_mask");
			if ((buttonMask & MAIN_BUTTONS) != 0) {
				Vector2 relative = (Vector2) ev.getProperty("screen_relative");
				rotY -= relative.getX() * ROT_SPEED;
				rotY = clamp(rotY, -1.95, 1.95);
				rotX -= relative.getY() * ROT_SPEED;
				rotX = clamp(rotX, -1.4, 0.45);
				if (cameraHolder != null) cameraHolder.call("set_rotation", new Vector3(0, rotY, 0));
				if (rotationX != null) rotationX.call("set_rotation", new Vector3(rotX, 0, 0));
				return true;
			}
		}
		return false;
	}

	@Override
	public void _process(double delta) {
		if (testers == null || cameraHolder == null) return;
		org.godot.Godot currentTester = (org.godot.Godot) testers.call("get_child", testerIndex);
		if (currentTester == null) return;

		// Horizontal lerp (X axis)
		Vector3 testerOrigin = (Vector3) currentTester.getProperty("position");
		Vector3 holderOrigin = (Vector3) cameraHolder.getProperty("position");
		if (testerOrigin != null && holderOrigin != null) {
			double newX = lerp(holderOrigin.getX(), testerOrigin.getX(), INTERP_SPEED * delta);
			cameraHolder.setProperty("position", new Vector3(newX, holderOrigin.getY(), holderOrigin.getZ()));
		}
	}

	@GodotMethod
	public void onPreviousPressed() { if (testerIndex > 0) { testerIndex--; updateGui(); } }
	@GodotMethod
	public void onNextPressed() {
		if (testers != null) {
			int count = (int) (long) testers.call("get_child_count");
			if (testerIndex < count - 1) testerIndex++;
		}
		updateGui();
	}

	private void updateGui() {
		if (testers == null) return;
		org.godot.Godot currentTester = (org.godot.Godot) testers.call("get_child", testerIndex);
		if (currentTester == null) return;
		String name = (String) currentTester.call("get_name");

		if (materialName != null) materialName.setProperty("text", name);
		org.godot.Godot prevBtn = (org.godot.Godot) call("get_node", "UI/Previous");
		org.godot.Godot nextBtn = (org.godot.Godot) call("get_node", "UI/Next");
		if (prevBtn != null) prevBtn.setProperty("disabled", testerIndex == 0);
		if (nextBtn != null) {
			int count = (int) (long) testers.call("get_child_count");
			nextBtn.setProperty("disabled", testerIndex == count - 1);
		}
	}

	private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
	private static double lerp(double from, double to, double w) { return from + (to - from) * w; }
}
