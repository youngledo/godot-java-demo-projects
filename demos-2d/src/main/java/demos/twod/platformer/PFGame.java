package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.node.Node;
import org.godot.node.SceneTree;

@GodotClass(name = "PFGame", parent = "Node")
public class PFGame extends Node {

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		if ((boolean) ev.isActionPressed("toggle_pause")) {
			org.godot.node.SceneTree tree = getTree();
			if (tree != null) {
				boolean paused = (boolean) tree.getProperty("paused");
				tree.setProperty("paused", !paused);
			}
		}
		return false;
	}
}
