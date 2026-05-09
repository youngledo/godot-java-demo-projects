package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.RigidBody2D;

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
		setDeferred("monitoring", false);
	}

	@GodotMethod
	public void OnBodyEntered(Object body) {
		if (body instanceof PFEnemy enemy) {
			enemy.destroy();
		}

		org.godot.node.AnimationPlayer anim = getNodeAs("AnimationPlayer", org.godot.node.AnimationPlayer.class);
		if (anim != null) anim.play("destroy");
	}
}
