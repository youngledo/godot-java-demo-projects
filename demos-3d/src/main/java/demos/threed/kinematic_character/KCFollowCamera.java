package demos.threed.kinematic_character;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.math.Vector3;
import org.godot.node.Camera3D;

@GodotClass(name = "KCFollowCamera", parent = "Camera3D")
public class KCFollowCamera extends Camera3D {

	@Export
	public double minDistance = 0.5;
	@Export
	public double maxDistance = 3.0;
	@Export
	public double angleVAdjust = 0.0;

	private static final double MAX_HEIGHT = 2.0;
	private static final double MIN_HEIGHT = 0;

	private org.godot.node.Node3D targetNode;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		targetNode = (org.godot.node.Node3D) getParent();
		// Set top_level = true to detach from parent
		setProperty("top_level", true);
	}

	@Override
	public void _physicsProcess(double delta) {
		if (targetNode == null) return;

		Vector3 targetPos = (Vector3) targetNode.getGlobalPosition();
		Vector3 cameraPos = (Vector3) call("get_global_position");

		if (targetPos == null || cameraPos == null) return;

		Vector3 deltaPos = new Vector3(
			cameraPos.getX() - targetPos.getX(),
			cameraPos.getY() - targetPos.getY(),
			cameraPos.getZ() - targetPos.getZ()
		);

		double len = Math.sqrt(deltaPos.getX() * deltaPos.getX() + deltaPos.getY() * deltaPos.getY() + deltaPos.getZ() * deltaPos.getZ());

		if (len < minDistance && len > 0) {
			deltaPos = new Vector3(deltaPos.getX() / len * minDistance, deltaPos.getY() / len * minDistance, deltaPos.getZ() / len * minDistance);
		} else if (len > maxDistance && len > 0) {
			deltaPos = new Vector3(deltaPos.getX() / len * maxDistance, deltaPos.getY() / len * maxDistance, deltaPos.getZ() / len * maxDistance);
		}

		double clampedY = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, deltaPos.getY()));
		deltaPos = new Vector3(deltaPos.getX(), clampedY, deltaPos.getZ());

		Vector3 newCamPos = new Vector3(targetPos.getX() + deltaPos.getX(), targetPos.getY() + deltaPos.getY(), targetPos.getZ() + deltaPos.getZ());
		lookAtFromPosition(newCamPos, targetPos, new Vector3(0, 1, 0));
	}
}
