package demos.threed.physics_interpolation;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.RigidBody3D;
import org.godot.node.Node;

@GodotClass(name = "PIBullet", parent = "RigidBody3D")
public class PIBullet extends RigidBody3D {

	@Export
	public double lifeTime = 3.0;

	private boolean enabled = false;
	private double timer = 0;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		org.godot.node.CollisionShape3D col = (org.godot.node.CollisionShape3D) getNode("CollisionShape3D");
		if (col != null) col.setProperty("disabled", true);

		timer = lifeTime;
	}

	@Override
	public void _physicsProcess(double delta) {
		if (!enabled) {
			org.godot.node.CollisionShape3D col = (org.godot.node.CollisionShape3D) getNode("CollisionShape3D");
			if (col != null) col.setProperty("disabled", false);
			enabled = true;
		}

		// Animate scale
		double ratio = 1.0 - timer / lifeTime;
		double scale = 1.0 - ratio * 0.5; // Shrink over time
		org.godot.node.Node scaler = getNode("Scaler");
		if (scaler != null) {
			scaler.setProperty("scale", new Vector3(scale, scale, scale));
		}

		timer -= delta;
		if (timer <= 0) {
			queueFree();
		}
	}
}
