package demos.twod.navigation;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;
import org.godot.node.NavigationAgent2D;
import org.godot.singleton.Input;

@GodotClass(name = "NavCharacter", parent = "CharacterBody2D")
public class NavCharacter extends CharacterBody2D {

	private static final double MOVEMENT_SPEED = 200.0;
	private NavigationAgent2D navigationAgent;

	@Override
	public void _ready() {
		navigationAgent = getNodeAs("NavigationAgent2D", NavigationAgent2D.class);
		if (navigationAgent != null) {
			navigationAgent.setPathDesiredDistance(2.0);
			navigationAgent.setTargetDesiredDistance(2.0);
			navigationAgent.setDebugEnabled(true);
		}
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		boolean pressed = Input.singleton().isActionJustPressed("click");
		if (pressed) {
			Vector2 mousePos = getGlobalMousePosition();
			if (navigationAgent != null) {
				navigationAgent.setTargetPosition(mousePos);
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
		boolean finished = navigationAgent.isNavigationFinished();
		if (finished) {
			return;
		}
		Vector2 currentPos = getGlobalPosition();
		Vector2 nextPos = navigationAgent.getNextPathPosition();
		double dx = nextPos.getX() - currentPos.getX();
		double dy = nextPos.getY() - currentPos.getY();
		double len = Math.sqrt(dx * dx + dy * dy);
		if (len > 0) {
			dx /= len;
			dy /= len;
		}
		setProperty("velocity", new Vector2(dx * MOVEMENT_SPEED, dy * MOVEMENT_SPEED));
		moveAndSlide();
	}
}
