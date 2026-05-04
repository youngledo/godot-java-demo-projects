package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "PFPauseMenu", parent = "Control")
public class PFPauseMenu extends Control {

	private org.godot.Godot coinsCounter;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		call("hide");
		org.godot.Godot colorRect = (org.godot.Godot) call("get_node", "ColorRect");
		if (colorRect != null) {
			coinsCounter = (org.godot.Godot) colorRect.call("get_node", "CoinsCounter");
		}
	}

	@GodotMethod
	public void open() {
		call("show");
	}

	@GodotMethod
	public void close() {
		org.godot.Godot tree = (org.godot.Godot) call("get_tree");
		if (tree != null) tree.setProperty("paused", false);
		call("hide");
	}

	@GodotMethod
	public void _on_coin_collected() {
		if (coinsCounter != null) coinsCounter.call("collect_coin");
	}

	@GodotMethod
	public void _on_resume_button_pressed() {
		close();
	}

	@GodotMethod
	public void _on_quit_button_pressed() {
		org.godot.Godot tree = (org.godot.Godot) call("get_tree");
		if (tree != null) tree.call("quit");
	}
}
