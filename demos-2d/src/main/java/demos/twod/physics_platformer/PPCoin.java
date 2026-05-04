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
	public void _on_body_entered(Object body) {
		if (!taken) {
			org.godot.Godot b = (org.godot.Godot) body;
			String cls = (String) b.call("get_class");
			if ("PPPlayer".equals(cls)) {
				taken = true;
				org.godot.Godot anim = (org.godot.Godot) call("get_node", "AnimationPlayer");
				if (anim != null) anim.call("play", "taken");
			}
		}
	}
}
