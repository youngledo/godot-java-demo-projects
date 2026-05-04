package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.node.Node;

@GodotClass(name = "PFGame", parent = "Node")
public class PFGame extends Node {

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.Godot ev = (org.godot.Godot) inputEvent;
		if ((boolean) ev.call("is_action_pressed", "toggle_pause")) {
			org.godot.Godot tree = (org.godot.Godot) call("get_tree");
			if (tree != null) {
				boolean paused = (boolean) tree.getProperty("paused");
				tree.setProperty("paused", !paused);
			}
		}
		return false;
	}
}
