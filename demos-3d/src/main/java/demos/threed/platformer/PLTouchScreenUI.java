package demos.threed.platformer;

import org.godot.annotation.GodotClass;
import org.godot.node.CanvasLayer;

@GodotClass(name = "PLTouchScreenUI", parent = "CanvasLayer")
public class PLTouchScreenUI extends CanvasLayer {

	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		hide();
		org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
		if (ds != null) {
			Object available = ds.call("is_touchscreen_available");
			if (available != null && (boolean) available) {
				show();
			}
		}
	}
}
