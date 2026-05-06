package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.Area2D;
import org.godot.node.Node;

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
	public void OnBodyEntered(Object body) {
		org.godot.node.AnimationPlayer anim = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
		if (anim != null) anim.play("picked");

		// Notify player
		org.godot.Godot b = (org.godot.Godot) body;
		b.emitSignal("coin_collected");
	}
}
