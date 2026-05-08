package demos.twod.hexagonal_map;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;
import org.godot.singleton.Input;

@GodotClass(name = "Troll", parent = "CharacterBody2D")
public class Troll extends CharacterBody2D {

	private static final double MOTION_SPEED = 30.0;
	private static final double FRICTION_FACTOR = 0.89;
	private static final double TAN30DEG = Math.tan(Math.toRadians(30));

	@Override
	public void _physicsProcess(double delta) {
		Input input = Input.singleton();
		double motionX = input.getAxis("move_left", "move_right");
		double motionY = input.getAxis("move_up", "move_down") * TAN30DEG;

		double len = Math.sqrt(motionX * motionX + motionY * motionY);
		if (len > 0) {
			motionX /= len;
			motionY /= len;
		}

		Vector2 vel = (Vector2) getVelocity();
		double vx = vel.getX() + motionX * MOTION_SPEED;
		double vy = vel.getY() + motionY * MOTION_SPEED;
		vx *= FRICTION_FACTOR;
		vy *= FRICTION_FACTOR;
		setVelocity(new Vector2(vx, vy));
		moveAndSlide();
	}
}
