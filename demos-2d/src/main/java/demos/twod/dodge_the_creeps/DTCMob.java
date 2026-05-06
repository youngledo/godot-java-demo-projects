package demos.twod.dodge_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.RigidBody2D;
import org.godot.node.Node;

@GodotClass(name = "DTCMob", parent = "RigidBody2D")
public class DTCMob extends RigidBody2D {

	private org.godot.node.AnimatedSprite2D animatedSprite;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		animatedSprite = (org.godot.node.AnimatedSprite2D) getNode("AnimatedSprite2D");
		org.godot.node.VisibleOnScreenNotifier2D notifier = (org.godot.node.VisibleOnScreenNotifier2D) getNode("VisibleOnScreenNotifier2D");

		if (animatedSprite != null) {
			org.godot.node.SpriteFrames spriteFrames = (org.godot.node.SpriteFrames) animatedSprite.getProperty("sprite_frames");
			if (spriteFrames != null) {
				String[] animNames = (String[]) spriteFrames.call("get_animation_names");
				if (animNames != null && animNames.length > 0) {
					int idx = (int) (Math.random() * animNames.length);
					animatedSprite.call("set_animation", animNames[idx]);
				}
			}
			animatedSprite.play();
		}

		if (notifier != null) {
			org.godot.core.Callable cb = new org.godot.core.Callable(this, "_on_screen_exited");
			notifier.connect("screen_exited", cb);
		}
	}

	@GodotMethod
	public void OnScreenExited() {
		queueFree();
	}
}
