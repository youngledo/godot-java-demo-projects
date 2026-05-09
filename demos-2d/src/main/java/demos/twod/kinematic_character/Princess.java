package demos.twod.kinematic_character;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.Area2D;
import org.godot.node.Node;

@GodotClass(name = "Princess", parent = "Area2D")
public class Princess extends Area2D {

	@Override
	public void _ready() {
		connect("body_entered", new Callable(this, "_on_body_entered"), 0);
	}

	@GodotMethod
	public void OnBodyEntered(Object body) {
		String name = (String) ((org.godot.node.Node) body).getName();
		if ("KCPlayer".equals(name)) {
			org.godot.node.CanvasItem winText = (org.godot.node.CanvasItem) getNode("../WinText");
			if (winText != null) {
				winText.show();
			}
		}
	}
}
