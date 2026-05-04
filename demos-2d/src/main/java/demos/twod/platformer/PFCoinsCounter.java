package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;

@GodotClass(name = "PFCoinsCounter", parent = "Panel")
public class PFCoinsCounter extends Panel {

	private int coinsCollected = 0;
	private org.godot.Godot coinsLabel;
	private org.godot.Godot animatedSprite;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		coinsLabel = (org.godot.Godot) call("get_node", "Label");
		animatedSprite = (org.godot.Godot) call("get_node", "AnimatedSprite2D");

		if (coinsLabel != null) coinsLabel.setProperty("text", String.valueOf(coinsCollected));
		if (animatedSprite != null) animatedSprite.call("play");
	}

	@Override
	public void _exitTree() {
		if (animatedSprite != null) animatedSprite.call("stop");
		animatedSprite = null;
		coinsLabel = null;
	}

	@GodotMethod
	public void collectCoin() {
		coinsCollected++;
		if (coinsLabel != null) coinsLabel.setProperty("text", String.valueOf(coinsCollected));
	}
}
