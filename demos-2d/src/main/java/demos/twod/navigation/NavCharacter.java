package demos.twod.navigation;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;

@GodotClass(name = "NavCharacter", parent = "CharacterBody2D")
public class NavCharacter extends CharacterBody2D {

	private static final double MOVEMENT_SPEED = 200.0;
	private org.godot.Godot navigationAgent;

	@Override
	public void _ready() {
		navigationAgent = (org.godot.Godot) call("get_node", "NavigationAgent2D");
		if (navigationAgent != null) {
			navigationAgent.call("set_path_desired_distance", 2.0);
			navigationAgent.call("set_target_desired_distance", 2.0);
			navigationAgent.call("set_debug_enabled", true);
		}
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		boolean pressed = (boolean) call("Input.is_action_just_pressed", "click");
		if (pressed) {
			Vector2 mousePos = (Vector2) call("get_global_mouse_position");
			if (navigationAgent != null) {
				navigationAgent.call("set_target_position", mousePos);
			}
			return true;
		}
		return false;
	}

	@Override
	public void _physicsProcess(double delta) {
		if (navigationAgent == null) {
			return;
		}
		boolean finished = (boolean) navigationAgent.call("is_navigation_finished");
		if (finished) {
			return;
		}
		Vector2 currentPos = (Vector2) call("get_global_position");
		Vector2 nextPos = (Vector2) navigationAgent.call("get_next_path_position");
		double dx = nextPos.getX() - currentPos.getX();
		double dy = nextPos.getY() - currentPos.getY();
		double len = Math.sqrt(dx * dx + dy * dy);
		if (len > 0) {
			dx /= len;
			dy /= len;
		}
		setProperty("velocity", new Vector2(dx * MOVEMENT_SPEED, dy * MOVEMENT_SPEED));
		call("move_and_slide");
	}
}
