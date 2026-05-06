package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;
import org.godot.node.Node;

@GodotClass(name = "PFCoinsCounter", parent = "Panel")
public class PFCoinsCounter extends Panel {

	private int coinsCollected = 0;
	private org.godot.node.Label coinsLabel;
	private org.godot.node.AnimatedSprite2D animatedSprite;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		coinsLabel = (org.godot.node.Label) getNode("Label");
		animatedSprite = (org.godot.node.AnimatedSprite2D) getNode("AnimatedSprite2D");

		if (coinsLabel != null) coinsLabel.setProperty("text", String.valueOf(coinsCollected));
		if (animatedSprite != null) animatedSprite.play();
	}

	@Override
	public void _exitTree() {
		if (animatedSprite != null) animatedSprite.stop();
		animatedSprite = null;
		coinsLabel = null;
	}

	@GodotMethod
	public void collectCoin() {
		coinsCollected++;
		if (coinsLabel != null) coinsLabel.setProperty("text", String.valueOf(coinsCollected));
	}
}
