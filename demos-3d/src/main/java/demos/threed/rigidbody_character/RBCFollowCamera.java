package demos.threed.rigidbody_character;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.math.Vector3;
import org.godot.node.Camera3D;

@GodotClass(name = "RBCFollowCamera", parent = "Camera3D")
public class RBCFollowCamera extends Camera3D {

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
		setProperty("top_level", true);
	}

	@Override
	public void _physicsProcess(double delta) {
		if (targetNode == null) return;

		Vector3 targetPos = (Vector3) targetNode.getGlobalPosition();
		Vector3 cameraPos = (Vector3) call("get_global_position");
		if (targetPos == null || cameraPos == null) return;

		double dpx = cameraPos.getX() - targetPos.getX();
		double dpy = cameraPos.getY() - targetPos.getY();
		double dpz = cameraPos.getZ() - targetPos.getZ();
		double len = Math.sqrt(dpx * dpx + dpy * dpy + dpz * dpz);

		if (len < minDistance && len > 0) { dpx = dpx/len*minDistance; dpy = dpy/len*minDistance; dpz = dpz/len*minDistance; }
		else if (len > maxDistance && len > 0) { dpx = dpx/len*maxDistance; dpy = dpy/len*maxDistance; dpz = dpz/len*maxDistance; }

		dpy = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, dpy));

		Vector3 newCamPos = new Vector3(targetPos.getX() + dpx, targetPos.getY() + dpy, targetPos.getZ() + dpz);
		lookAtFromPosition(newCamPos, targetPos, new Vector3(0, 1, 0));
	}
}
