package demos.twod.platformer;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;

@GodotClass(name = "PFPlayer", parent = "CharacterBody2D")
public class PFPlayer extends CharacterBody2D {

	private static final double WALK_SPEED = 300.0;
	private static final double ACCELERATION = WALK_SPEED * 6.0;
	private static final double JUMP_VELOCITY = -725.0;
	private static final double TERMINAL_VELOCITY = 700.0;

	@Export
	public String actionSuffix = "";

	private boolean doubleJumpCharged = false;
	private org.godot.Godot sprite;
	private org.godot.Godot animationPlayer;
	private org.godot.Godot platformDetector;
	private org.godot.Godot camera;
	private org.godot.Godot gun;
	private org.godot.Godot jumpSound;
	private double gravity = 980.0;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		call("add_user_signal", "coin_collected");
		sprite = (org.godot.Godot) call("get_node", "Sprite2D");
		animationPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
		platformDetector = (org.godot.Godot) call("get_node", "PlatformDetector");
		camera = (org.godot.Godot) call("get_node", "Camera");
		jumpSound = (org.godot.Godot) call("get_node", "Jump");

		if (sprite != null) {
			gun = (org.godot.Godot) sprite.call("get_node", "Gun");
		}

		gravity = 980.0;
	}

	@Override
	public void _exitTree() {
		if (jumpSound != null) jumpSound.call("stop");
		jumpSound = null;
		sprite = null;
		animationPlayer = null;
		platformDetector = null;
		camera = null;
		gun = null;
	}

	@Override
	public void _physicsProcess(double delta) {
		if ((boolean) call("is_on_floor")) {
			doubleJumpCharged = true;
		}

		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
		String suffix = actionSuffix;

		if ((boolean) input.call("is_action_just_pressed", "jump" + suffix)) {
			tryJump(suffix);
		} else if (!(boolean) input.call("is_action_pressed", "jump" + suffix)) {
			Vector2 vel = (Vector2) getProperty("velocity");
			if (vel != null && vel.getY() < 0.0) {
				setProperty("velocity", new Vector2(vel.getX(), vel.getY() * 0.6));
			}
		}

		Vector2 vel = (Vector2) getProperty("velocity");
		if (vel == null) vel = new Vector2(0, 0);
		double vy = Math.min(TERMINAL_VELOCITY, vel.getY() + gravity * delta);

		double direction = 0;
		if ((boolean) input.call("is_action_pressed", "move_left" + suffix)) direction -= 1;
		if ((boolean) input.call("is_action_pressed", "move_right" + suffix)) direction += 1;
		direction *= WALK_SPEED;

		double vx = vel.getX();
		vx = moveToward(vx, direction, ACCELERATION * delta);

		if (Math.abs(vx) > 0.1 && sprite != null) {
			sprite.setProperty("scale", new Vector2(vx > 0 ? 1.0 : -1.0, 1.0));
		}

		setProperty("velocity", new Vector2(vx, vy));
		call("move_and_slide");

		// Animation
		String anim = getNewAnimation(false);
		if (animationPlayer != null) {
			String currentAnim = (String) animationPlayer.getProperty("current_animation");
			if (!anim.equals(currentAnim)) {
				animationPlayer.call("play", anim);
			}
		}
	}

	@GodotMethod
	public void tryJump(String suffix) {
		if ((boolean) call("is_on_floor")) {
			Vector2 vel = (Vector2) getProperty("velocity");
			setProperty("velocity", new Vector2(vel != null ? vel.getX() : 0, JUMP_VELOCITY));
			if (jumpSound != null) jumpSound.call("play");
		} else if (doubleJumpCharged) {
			doubleJumpCharged = false;
			Vector2 vel = (Vector2) getProperty("velocity");
			double vx = (vel != null ? vel.getX() : 0) * 2.5;
			setProperty("velocity", new Vector2(vx, JUMP_VELOCITY));
			if (jumpSound != null) {
				jumpSound.setProperty("pitch_scale", 1.5);
				jumpSound.call("play");
			}
		}
	}

	private String getNewAnimation(boolean isShooting) {
		String anim;
		if ((boolean) call("is_on_floor")) {
			Vector2 vel = (Vector2) getProperty("velocity");
			if (vel != null && Math.abs(vel.getX()) > 0.1) {
				anim = "run";
			} else {
				anim = "idle";
			}
		} else {
			Vector2 vel = (Vector2) getProperty("velocity");
			if (vel != null && vel.getY() > 0.0) {
				anim = "falling";
			} else {
				anim = "jumping";
			}
		}
		return anim;
	}

	private double moveToward(double current, double target, double maxDelta) {
		if (Math.abs(target - current) <= maxDelta) return target;
		return current + Math.signum(target - current) * maxDelta;
	}
}
