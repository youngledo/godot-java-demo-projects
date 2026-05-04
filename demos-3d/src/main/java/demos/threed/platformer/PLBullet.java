package demos.threed.platformer;

import org.godot.annotation.GodotClass;
import org.godot.node.RigidBody3D;

@GodotClass(name = "PLBullet", parent = "RigidBody3D")
public class PLBullet extends RigidBody3D {

	private boolean enabled = true;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;
	}
}
