package demos.twod.bullet_shower;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.Node2D;
import org.godot.node.Node;
import org.godot.singleton.Input;

@GodotClass(name = "BSPlayer", parent = "Node2D")
public class BSPlayer extends Node2D {

	private int touching = 0;
	private org.godot.node.AnimatedSprite2D sprite;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		sprite = (org.godot.node.AnimatedSprite2D) getNode("AnimatedSprite2D");
		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
		input.setMouseMode(1); // MOUSE_MODE_HIDDEN
	}

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		String className = ev.get_class_();

		if ("InputEventMouseMotion".equals(className)) {
			Vector2 evPos = (Vector2) ev.getProperty("position");
			if (evPos != null) {
				setProperty("position", new Vector2(evPos.getX(), evPos.getY() - 16));
			}
		}
		return false;
	}

	@GodotMethod
	public void OnBodyShapeEntered(Object bodyId, Object body, long bodyShapeIndex, long localShapeIndex) {
		touching++;
		if (touching >= 1 && sprite != null) {
			sprite.setProperty("frame", 1);
		}
	}

	@GodotMethod
	public void OnBodyShapeExited(Object bodyId, Object body, long bodyShapeIndex, long localShapeIndex) {
		touching--;
		if (touching == 0 && sprite != null) {
			sprite.setProperty("frame", 0);
		}
	}
}
