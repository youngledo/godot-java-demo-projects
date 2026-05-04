package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;

@GodotClass(name = "PFEnemy", parent = "CharacterBody2D")
public class PFEnemy extends CharacterBody2D {

	private static final double WALK_SPEED = 22.0;

	private int state = 0; // 0=WALKING, 1=DEAD
	private double gravity = 980.0;
	private org.godot.Godot sprite;
	private org.godot.Godot animationPlayer;
	private org.godot.Godot floorDetectorLeft;
	private org.godot.Godot floorDetectorRight;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		sprite = (org.godot.Godot) call("get_node", "Sprite2D");
		animationPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
		floorDetectorLeft = (org.godot.Godot) call("get_node", "FloorDetectorLeft");
		floorDetectorRight = (org.godot.Godot) call("get_node", "FloorDetectorRight");
	}

	@Override
	public void _physicsProcess(double delta) {
		Vector2 vel = (Vector2) getProperty("velocity");
		if (vel == null) vel = new Vector2(0, 0);

		if (state == 0 && Math.abs(vel.getX()) < 0.01) {
			vel = new Vector2(WALK_SPEED, vel.getY());
		}

		vel = new Vector2(vel.getX(), vel.getY() + gravity * delta);

		if (floorDetectorLeft != null && !(boolean) floorDetectorLeft.call("is_colliding")) {
			vel = new Vector2(WALK_SPEED, vel.getY());
		} else if (floorDetectorRight != null && !(boolean) floorDetectorRight.call("is_colliding")) {
			vel = new Vector2(-WALK_SPEED, vel.getY());
		}

		if ((boolean) call("is_on_wall")) {
			vel = new Vector2(-vel.getX(), vel.getY());
		}

		setProperty("velocity", vel);
		call("move_and_slide");

		vel = (Vector2) getProperty("velocity");
		if (vel != null && sprite != null) {
			sprite.setProperty("scale", new Vector2(vel.getX() > 0 ? 0.8 : -0.8, 0.8));
		}

		String anim = getNewAnimation();
		if (animationPlayer != null) {
			String current = (String) animationPlayer.getProperty("current_animation");
			if (!anim.equals(current)) animationPlayer.call("play", anim);
		}
	}

	private String getNewAnimation() {
		if (state == 1) return "destroy";
		Vector2 vel = (Vector2) getProperty("velocity");
		if (vel != null && Math.abs(vel.getX()) > 0.1) return "walk";
		return "idle";
	}

	@Override
	public void _exitTree() {
		if (animationPlayer != null) {
			animationPlayer.call("stop");
		}
	}

	@GodotMethod
	public void destroy() {
		state = 1;
		setProperty("velocity", new Vector2(0, 0));
	}
}
