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
	public void _on_body_entered(Object body) {
		String name = (String) ((org.godot.Godot) body).call("get_name");
		if ("KCPlayer".equals(name)) {
			Node winText = (Node) call("get_node", "../WinText");
			if (winText != null) {
				winText.call("show");
			}
		}
	}
}
