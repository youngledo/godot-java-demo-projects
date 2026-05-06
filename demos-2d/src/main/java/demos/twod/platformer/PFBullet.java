package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.RigidBody2D;
import org.godot.node.Node;

@GodotClass(name = "PFBullet", parent = "RigidBody2D")
public class PFBullet extends RigidBody2D {

	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		connect("body_entered", new Callable(this, "_on_body_entered"), 0);
	}

	@Override
	public void _exitTree() {
		call("set_deferred", "monitoring", false);
	}

	@GodotMethod
	public void OnBodyEntered(Object body) {
		org.godot.Godot b = (org.godot.Godot) body;
		String cls = (String) b.call("get_class");
		if ("PFEnemy".equals(cls)) {
			b.call("destroy");
		}

		org.godot.node.AnimationPlayer anim = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
		if (anim != null) anim.play("destroy");
	}
}
