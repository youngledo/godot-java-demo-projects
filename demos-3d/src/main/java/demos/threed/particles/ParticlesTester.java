package demos.threed.particles;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.WorldEnvironment;
import org.godot.node.Node;

@GodotClass(name = "ParticlesTester", parent = "WorldEnvironment")
public class ParticlesTester extends WorldEnvironment {

	private static final double ROT_SPEED = 0.003;
	private static final double ZOOM_SPEED = 0.125;
	private static final int MAIN_BUTTONS = 1 | 2 | 4;

	private int testerIndex = 0;
	private double rotX = Math.toRadians(-22.5);
	private double rotY = Math.toRadians(90);
	private double zoom = 2.5;

	private org.godot.node.Node testers;
	private org.godot.node.Camera3D cameraHolder;
	private org.godot.node.Node3D rotationX;
	private org.godot.node.Camera3D camera;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		testers = getNode("Testers");
		cameraHolder = (org.godot.node.Camera3D) getNode("CameraHolder");
		rotationX = getNodeAs("CameraHolder/RotationX", org.godot.node.Node3D.class);
		camera = (org.godot.node.Camera3D) getNode("CameraHolder/RotationX/Camera3D");

		if (cameraHolder != null) cameraHolder.setRotation(new Vector3(0, rotY, 0));
		if (rotationX != null) rotationX.setRotation( new Vector3(rotX, 0, 0));
		updateGui();
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		String className = ev.get_class_();

		if ((boolean) ev.isActionPressed("ui_left")) { onPreviousPressed(); return true; }
		if ((boolean) ev.isActionPressed("ui_right")) { onNextPressed(); return true; }

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
				if (cameraHolder != null) cameraHolder.setRotation(new Vector3(0, rotY, 0));
				if (rotationX != null) rotationX.setRotation( new Vector3(rotX, 0, 0));
				return true;
			}
		}
		return false;
	}

	@Override
	public void _process(double delta) {
		if (testers == null || cameraHolder == null || camera == null) return;
		org.godot.node.Node currentTester = (org.godot.node.Node) testers.getChild(testerIndex);
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
			int count = (int) (long) testers.getChildCount();
			testerIndex = Math.min(testerIndex + 1, count - 1);
		}
		updateGui();
	}

	private void updateGui() {
		if (testers == null) return;
		org.godot.node.Node currentTester = (org.godot.node.Node) testers.getChild(testerIndex);
		if (currentTester == null) return;
		String name = (String) currentTester.getName();

		org.godot.node.Node testName = getNode("TestName");
		if (testName != null) testName.setProperty("text", capitalize(name));
		org.godot.node.Node prevBtn = getNode("Previous");
		org.godot.node.Node nextBtn = getNode("Next");
		if (prevBtn != null) prevBtn.setProperty("disabled", testerIndex == 0);
		if (nextBtn != null) {
			int count = (int) (long) testers.getChildCount();
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
