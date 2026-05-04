package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.node.Node2D;

@GodotClass(name = "PFLevel", parent = "Node2D")
public class PFLevel extends Node2D {

	private static final double LIMIT_LEFT = -315;
	private static final double LIMIT_TOP = -250;
	private static final double LIMIT_RIGHT = 955;
	private static final double LIMIT_BOTTOM = 690;

	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		long childCount = (long) call("get_child_count");
		for (int i = 0; i < childCount; i++) {
			org.godot.Godot child = (org.godot.Godot) call("get_child", i);
			if (child == null) continue;
			String cls = (String) child.call("get_class");
			if ("PFPlayer".equals(cls)) {
				org.godot.Godot cam = (org.godot.Godot) child.call("get_node", "Camera");
				if (cam != null) {
					cam.call("set_limit", 0, LIMIT_LEFT);
					cam.call("set_limit", 1, LIMIT_TOP);
					cam.call("set_limit", 2, LIMIT_RIGHT);
					cam.call("set_limit", 3, LIMIT_BOTTOM);
				}
			}
		}
	}
}
