package demos.twod.physics_platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.Area2D;

@GodotClass(name = "PPCoin", parent = "Area2D")
public class PPCoin extends Area2D {

	private boolean taken = false;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		connect("body_entered", new Callable(this, "_on_body_entered"), 0);
	}

	@GodotMethod
	public void OnBodyEntered(Object body) {
		if (!taken && body instanceof PPPlayer) {
			taken = true;
			org.godot.node.AnimationPlayer anim = getNodeAs("AnimationPlayer", org.godot.node.AnimationPlayer.class);
			if (anim != null) anim.play("taken");
		}
	}
}
