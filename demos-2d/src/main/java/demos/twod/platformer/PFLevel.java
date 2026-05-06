package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.node.Node2D;
import org.godot.node.Node;

@GodotClass(name = "PFLevel", parent = "Node2D")
public class PFLevel extends Node2D {

	private static final long LIMIT_LEFT = -315;
	private static final long LIMIT_TOP = -250;
	private static final long LIMIT_RIGHT = 955;
	private static final long LIMIT_BOTTOM = 690;

	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		long childCount = (long) getChildCount();
		for (int i = 0; i < childCount; i++) {
			org.godot.node.Node child = getChild(i);
			if (child == null) continue;
			String cls = child.get_class_();
			if ("PFPlayer".equals(cls)) {
				org.godot.node.Camera2D cam = (org.godot.node.Camera2D) child.getNode("Camera");
				if (cam != null) {
					cam.setLimit(0, LIMIT_LEFT);
					cam.setLimit(1, LIMIT_TOP);
					cam.setLimit(2, LIMIT_RIGHT);
					cam.setLimit(3, LIMIT_BOTTOM);
				}
			}
		}
	}
}
