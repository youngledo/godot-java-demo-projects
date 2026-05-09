package demos.twod.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;
import org.godot.node.RayCast2D;

@GodotClass(name = "PFEnemy", parent = "CharacterBody2D")
public class PFEnemy extends CharacterBody2D {

	private static final double WALK_SPEED = 22.0;

	private int state = 0; // 0=WALKING, 1=DEAD
	private double gravity = 980.0;
	private org.godot.node.Sprite2D sprite;
	private org.godot.node.AnimationPlayer animationPlayer;
	private RayCast2D floorDetectorLeft;
	private RayCast2D floorDetectorRight;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		sprite = getNodeAs("Sprite2D", org.godot.node.Sprite2D.class);
		animationPlayer = getNodeAs("AnimationPlayer", org.godot.node.AnimationPlayer.class);
		floorDetectorLeft = getNodeAs("FloorDetectorLeft", RayCast2D.class);
		floorDetectorRight = getNodeAs("FloorDetectorRight", RayCast2D.class);
	}

	@Override
	public void _physicsProcess(double delta) {
		Vector2 vel = (Vector2) getProperty("velocity");
		if (vel == null) vel = new Vector2(0, 0);

		if (state == 0 && Math.abs(vel.getX()) < 0.01) {
			vel = new Vector2(WALK_SPEED, vel.getY());
		}

		vel = new Vector2(vel.getX(), vel.getY() + gravity * delta);

		if (floorDetectorLeft != null && !(boolean) floorDetectorLeft.isColliding()) {
			vel = new Vector2(WALK_SPEED, vel.getY());
		} else if (floorDetectorRight != null && !(boolean) floorDetectorRight.isColliding()) {
			vel = new Vector2(-WALK_SPEED, vel.getY());
		}

		if ((boolean) isOnWall()) {
			vel = new Vector2(-vel.getX(), vel.getY());
		}

		setProperty("velocity", vel);
		moveAndSlide();

		vel = (Vector2) getProperty("velocity");
		if (vel != null && sprite != null) {
			sprite.setProperty("scale", new Vector2(vel.getX() > 0 ? 0.8 : -0.8, 0.8));
		}

		String anim = getNewAnimation();
		if (animationPlayer != null) {
			String current = (String) animationPlayer.getProperty("current_animation");
			if (!anim.equals(current)) animationPlayer.play(anim);
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
			animationPlayer.stop();
		}
	}

	@GodotMethod
	public void destroy() {
		state = 1;
		setProperty("velocity", new Vector2(0, 0));
	}
}
