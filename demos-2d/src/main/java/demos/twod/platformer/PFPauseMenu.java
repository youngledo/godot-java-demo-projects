package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;
import org.godot.node.SceneTree;

@GodotClass(name = "PFPauseMenu", parent = "Control")
public class PFPauseMenu extends Control {

	private PFCoinsCounter coinsCounter;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		hide();
		org.godot.node.ColorRect colorRect = (org.godot.node.ColorRect) getNode("ColorRect");
		if (colorRect != null) {
			coinsCounter = (PFCoinsCounter) colorRect.getNode("CoinsCounter");
		}
	}

	@GodotMethod
	public void open() {
		show();
	}

	@GodotMethod
	public void close() {
		org.godot.node.SceneTree tree = getTree();
		if (tree != null) tree.setProperty("paused", false);
		hide();
	}

	@GodotMethod
	public void OnCoinCollected() {
		if (coinsCounter != null) coinsCounter.collectCoin();
	}

	@GodotMethod
	public void OnResumeButtonPressed() {
		close();
	}

	@GodotMethod
	public void OnQuitButtonPressed() {
		org.godot.node.SceneTree tree = getTree();
		if (tree != null) tree.quit();
	}
}
