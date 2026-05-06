package demos.threed.truck_town;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.math.Vector3;
import org.godot.node.Camera3D;
import org.godot.node.Node;

@GodotClass(name = "TTFollowCamera", parent = "Camera3D")
public class TTFollowCamera extends Camera3D {

	private static final double FOV_SPEED_FACTOR = 60.0;
	private static final double FOV_SMOOTH_FACTOR = 0.2;
	private static final double FOV_CHANGE_MIN_SPEED = 0.05;

	@Export
	public double minDistance = 2.0;
	@Export
	public double maxDistance = 4.0;
	@Export
	public double height = 1.5;

	private int cameraType = 0; // 0=EXTERIOR, 1=INTERIOR, 2=TOP_DOWN
	private Vector3 initialTransformPos;
	private Vector3 initialTransformBasisX;
	private Vector3 initialTransformBasisY;
	private Vector3 initialTransformBasisZ;
	private double baseFov;
	private double desiredFov;
	private Vector3 previousPosition;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		baseFov = (double) getProperty("fov");
		desiredFov = baseFov;
		previousPosition = (Vector3) getProperty("global_position");
		if (previousPosition == null) previousPosition = new Vector3(0, 0, 0);

		// Store initial transform
		Object transform = getProperty("transform");
		if (transform != null) {
			initialTransformPos = (Vector3) ((org.godot.Godot) transform).getProperty("origin");
			Object basis = ((org.godot.Godot) transform).getProperty("basis");
			if (basis != null) {
				initialTransformBasisX = (Vector3) ((org.godot.Godot) basis).getProperty("x");
				initialTransformBasisY = (Vector3) ((org.godot.Godot) basis).getProperty("y");
				initialTransformBasisZ = (Vector3) ((org.godot.Godot) basis).getProperty("z");
			}
		}
	}

	@Override
	public void _physicsProcess(double delta) {
		if (cameraType == 0) { // EXTERIOR
			org.godot.Godot parent = (org.godot.Godot) getParent();
			if (parent == null) return;

			Object parentTransform = parent.getProperty("global_transform");
			if (parentTransform == null) return;
			Vector3 target = (Vector3) ((org.godot.Godot) parentTransform).getProperty("origin");

			Vector3 pos = (Vector3) getProperty("global_position");
			if (pos == null || target == null) return;

			Vector3 fromTarget = new Vector3(pos.getX() - target.getX(), pos.getY() - target.getY(), pos.getZ() - target.getZ());
			double dist = Math.sqrt(fromTarget.getX()*fromTarget.getX() + fromTarget.getY()*fromTarget.getY() + fromTarget.getZ()*fromTarget.getZ());

			if (dist < minDistance || dist > maxDistance) {
				double clampDist = Math.max(minDistance, Math.min(maxDistance, dist));
				if (dist > 0.001) {
					fromTarget = new Vector3(fromTarget.getX()/dist*clampDist, fromTarget.getY()/dist*clampDist, fromTarget.getZ()/dist*clampDist);
				}
			}

			Vector3 newPos = new Vector3(target.getX() + fromTarget.getX(), target.getY() + height, target.getZ() + fromTarget.getZ());
			lookAtFromPosition(newPos, target, new Vector3(0, 1, 0));
		} else if (cameraType == 2) { // TOP_DOWN
			org.godot.Godot parent = (org.godot.Godot) getParent();
			if (parent != null) {
				Vector3 parentPos = (Vector3) parent.getProperty("global_position");
				if (parentPos != null) {
					Vector3 curPos = (Vector3) getProperty("position");
					if (curPos != null) {
						setProperty("position", new Vector3(parentPos.getX(), curPos.getY(), parentPos.getZ()));
					}
				}
			}
			setProperty("rotation_degrees", new Vector3(270, 180, 0));
		}

		// Dynamic FOV
		Vector3 curGlobalPos = (Vector3) getProperty("global_position");
		if (curGlobalPos != null && previousPosition != null) {
			double posLen = Math.sqrt(curGlobalPos.getX()*curGlobalPos.getX() + curGlobalPos.getY()*curGlobalPos.getY() + curGlobalPos.getZ()*curGlobalPos.getZ());
			double prevLen = Math.sqrt(previousPosition.getX()*previousPosition.getX() + previousPosition.getY()*previousPosition.getY() + previousPosition.getZ()*previousPosition.getZ());
			double speedDiff = Math.abs(posLen - prevLen);
			desiredFov = Math.max(baseFov, Math.min(100.0, baseFov + (speedDiff - FOV_CHANGE_MIN_SPEED) * FOV_SPEED_FACTOR));
		}

		double currentFov = (double) getProperty("fov");
		double newFov = currentFov + (desiredFov - currentFov) * FOV_SMOOTH_FACTOR;
		setProperty("fov", newFov);

		previousPosition = curGlobalPos;
	}

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		if ((boolean) ev.isActionPressed("cycle_camera")) {
			cameraType = (cameraType + 1) % 3;
			if (cameraType == 0) {
				// Reset to initial transform
				if (initialTransformPos != null) {
					setProperty("global_position", initialTransformPos);
				}
			} else if (cameraType == 1) {
				// Interior camera
				org.godot.node.Node interior = getNode("../../InteriorCameraPosition");
				if (interior != null) {
					Object gt = interior.getProperty("global_transform");
					if (gt != null) {
						setProperty("global_transform", gt);
					}
				}
			} else if (cameraType == 2) {
				// Top-down
				org.godot.node.Node topDown = getNode("../../TopDownCameraPosition");
				if (topDown != null) {
					Object gt = topDown.getProperty("global_transform");
					if (gt != null) {
						setProperty("global_transform", gt);
					}
				}
			}
		}
		return false;
	}
}
