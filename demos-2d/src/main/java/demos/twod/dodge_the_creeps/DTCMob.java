package demos.twod.dodge_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.RigidBody2D;

@GodotClass(name = "DTCMob", parent = "RigidBody2D")
public class DTCMob extends RigidBody2D {

	private org.godot.Godot animatedSprite;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		animatedSprite = (org.godot.Godot) call("get_node", "AnimatedSprite2D");
		org.godot.Godot notifier = (org.godot.Godot) call("get_node", "VisibleOnScreenNotifier2D");

		if (animatedSprite != null) {
			org.godot.Godot spriteFrames = (org.godot.Godot) animatedSprite.getProperty("sprite_frames");
			if (spriteFrames != null) {
				String[] animNames = (String[]) spriteFrames.call("get_animation_names");
				if (animNames != null && animNames.length > 0) {
					int idx = (int) (Math.random() * animNames.length);
					animatedSprite.call("set_animation", animNames[idx]);
				}
			}
			animatedSprite.call("play");
		}

		if (notifier != null) {
			org.godot.core.Callable cb = new org.godot.core.Callable(this, "_on_screen_exited");
			notifier.connect("screen_exited", cb, 0);
		}
	}

	@GodotMethod
	public void _on_screen_exited() {
		call("queue_free");
	}
}
