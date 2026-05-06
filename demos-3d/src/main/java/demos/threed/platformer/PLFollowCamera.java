package demos.threed.platformer;

import org.godot.annotation.GodotClass;
import org.godot.node.Camera3D;
import org.godot.math.Vector3;
import org.godot.math.Basis;
import org.godot.math.Transform3D;

@GodotClass(name = "PLFollowCamera", parent = "Camera3D")
public class PLFollowCamera extends Camera3D {

	private static final double MAX_HEIGHT = 2.0;
	private static final double MIN_HEIGHT = 0.0;

	private double minDistance = 0.5;
	private double maxDistance = 3.5;
	private double angleVAdjust = 0.0;
	private double autoturnSpeed = 50.0;

	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;
		setAsTopLevel(true);
	}

	@Override
	public void _physicsProcess(double delta) {
		org.godot.node.Node3D parent = (org.godot.node.Node3D) getParent();
		if (parent == null) return;

		Object targetObj = parent.getGlobalTransform();
		Object posObj = call("get_global_transform");
		if (!(targetObj instanceof Transform3D) || !(posObj instanceof Transform3D)) return;

		Vector3 target = ((Transform3D) targetObj).getOrigin();
		Vector3 pos = ((Transform3D) posObj).getOrigin();
		Vector3 difference = pos.sub(target);

		double dist = difference.length();
		if (dist < minDistance && dist > 0.001) {
			difference = difference.normalized().mul(minDistance);
		} else if (dist > maxDistance) {
			difference = difference.normalized().mul(maxDistance);
		}

		double clampedY = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, difference.y));
		difference = new Vector3(difference.x, clampedY, difference.z);

		if (difference.lengthSquared() < 0.00001) {
			difference = pos.sub(target).normalized().mul(0.0001);
		}

		pos = target.add(difference);
		lookAtFromPosition(pos, target, Vector3.UP);

		Object xformObj = getProperty("transform");
		if (xformObj instanceof Transform3D) {
			Transform3D xform = (Transform3D) xformObj;
			Basis xformBasis = xform.getBasis();
			// Rotate around local X axis by angleVAdjust
			Basis rotBasis = Basis.fromAxisAngleX(Math.toRadians(angleVAdjust));
			Basis newBasis = rotBasis.multiply(xformBasis);
			setProperty("transform", new Transform3D(newBasis, xform.getOrigin()));
		}
	}
}
