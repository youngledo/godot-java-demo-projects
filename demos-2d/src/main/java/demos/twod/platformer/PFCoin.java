package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.Area2D;

@GodotClass(name = "PFCoin", parent = "Area2D")
public class PFCoin extends Area2D {

	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		connect("body_entered", new Callable(this, "_on_body_entered"), 0);
	}

	@GodotMethod
	public void _on_body_entered(Object body) {
		org.godot.Godot anim = (org.godot.Godot) call("get_node", "AnimationPlayer");
		if (anim != null) anim.call("play", "picked");

		// Notify player
		org.godot.Godot b = (org.godot.Godot) body;
		b.call("emit_signal", "coin_collected");
	}
}
