package demos.twod.bullet_shower;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.Node2D;

@GodotClass(name = "BSPlayer", parent = "Node2D")
public class BSPlayer extends Node2D {

	private int touching = 0;
	private org.godot.Godot sprite;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		sprite = (org.godot.Godot) call("get_node", "AnimatedSprite2D");
		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
		input.call("set_mouse_mode", 1); // MOUSE_MODE_HIDDEN
	}

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.Godot ev = (org.godot.Godot) inputEvent;
		String className = (String) ev.call("get_class");

		if ("InputEventMouseMotion".equals(className)) {
			Vector2 evPos = (Vector2) ev.getProperty("position");
			if (evPos != null) {
				setProperty("position", new Vector2(evPos.getX(), evPos.getY() - 16));
			}
		}
		return false;
	}

	@GodotMethod
	public void _on_body_shape_entered(Object bodyId, Object body, long bodyShapeIndex, long localShapeIndex) {
		touching++;
		if (touching >= 1 && sprite != null) {
			sprite.setProperty("frame", 1);
		}
	}

	@GodotMethod
	public void _on_body_shape_exited(Object bodyId, Object body, long bodyShapeIndex, long localShapeIndex) {
		touching--;
		if (touching == 0 && sprite != null) {
			sprite.setProperty("frame", 0);
		}
	}
}
