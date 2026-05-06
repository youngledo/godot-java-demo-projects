package demos.twod.kinematic_character;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;
import org.godot.singleton.Input;

@GodotClass(name = "KCPlayer", parent = "CharacterBody2D")
public class KCPlayer extends CharacterBody2D {

	private static final double WALK_FORCE = 600.0;
	private static final double WALK_MAX_SPEED = 200.0;
	private static final double STOP_FORCE = 1300.0;
	private static final double JUMP_SPEED = 200.0;
	private static final double GRAVITY = 500.0;

	@Override
	public void _physicsProcess(double delta) {
		Input input = Input.singleton();

		double walk = WALK_FORCE * input.getAxis("move_left", "move_right");
		Vector2 vel = (Vector2) getProperty("velocity");
		double vx = vel.getX();
		double vy = vel.getY();

		if (Math.abs(walk) < WALK_FORCE * 0.2) {
			vx = moveToward(vx, 0, STOP_FORCE * delta);
		} else {
			vx += walk * delta;
		}
		vx = clamp(vx, -WALK_MAX_SPEED, WALK_MAX_SPEED);
		vy += GRAVITY * delta;

		setProperty("velocity", new Vector2(vx, vy));
		moveAndSlide();

		boolean onFloor = (boolean) isOnFloor();
		if (onFloor && (boolean) (boolean) input.isActionJustPressed("jump")) {
			setProperty("velocity", new Vector2(vx, -JUMP_SPEED));
		}
	}

	private static double moveToward(double current, double target, double maxDelta) {
		if (Math.abs(target - current) <= maxDelta) return target;
		return current + Math.signum(target - current) * maxDelta;
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}
