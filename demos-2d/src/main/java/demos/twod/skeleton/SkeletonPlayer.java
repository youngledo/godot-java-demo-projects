package demos.twod.skeleton;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;
import org.godot.singleton.Input;
import org.godot.node.Node;

@GodotClass(name = "SkeletonPlayer", parent = "CharacterBody2D")
public class SkeletonPlayer extends CharacterBody2D {

	private static final String STATE_IDLE = "idle";
	private static final String STATE_WALK = "walk";
	private static final String STATE_RUN = "run";
	private static final String STATE_FLY = "fly";
	private static final String STATE_FALL = "fall";

	private static final double WALK_SPEED = 200.0;
	private static final double ACCELERATION_SPEED = WALK_SPEED * 6.0;
	private static final double JUMP_VELOCITY = -400.0;
	private static final double TERMINAL_VELOCITY = 400.0;
	private static final double GRAVITY = 900.0;

	private boolean fallingSlow = false;
	private boolean fallingFast = false;
	private double noMoveHorizontalTime = 0.0;
	private org.godot.node.Sprite2D sprite;
	private org.godot.node.Node animTree;
	private double spriteScaleX = 1.0;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		sprite = (org.godot.node.Sprite2D) getNode("Sprite2D");
		animTree = getNode("AnimationTree");
		if (sprite != null) {
			Vector2 scale = (Vector2) sprite.getProperty("scale");
			spriteScaleX = scale.getX();
		}
		if (animTree != null) {
			animTree.setProperty("active", true);
		}
	}

	@Override
	public void _physicsProcess(double delta) {
		Input input = Input.singleton();

		boolean isJumping = false;
		if ((boolean) (boolean) input.isActionJustPressed("jump")) {
			isJumping = tryJump();
		} else if (input.isActionJustReleased("jump", false)) {
			Vector2 vel = (Vector2) getProperty("velocity");
			if (vel.getY() < 0.0) {
				setProperty("velocity", new Vector2(vel.getX(), vel.getY() * 0.6));
			}
		}

		Vector2 vel = (Vector2) getProperty("velocity");
		double vx = vel.getX();
		double vy = Math.min(TERMINAL_VELOCITY, vel.getY() + GRAVITY * delta);

		double direction = input.getAxis("move_left", "move_right") * WALK_SPEED;
		vx = moveToward(vx, direction, ACCELERATION_SPEED * delta);

		if (noMoveHorizontalTime > 0.0) {
			vx = 0.0;
			noMoveHorizontalTime -= delta;
		}

		if (!isZeroApprox(vx)) {
			if (sprite != null) {
				double scaleX = (vx > 0 ? 1.0 : -1.0) * spriteScaleX;
				sprite.setProperty("scale", new Vector2(scaleX, spriteScaleX));
			}
		}

		setProperty("velocity", new Vector2(vx, vy));
		moveAndSlide();

		if (vy >= TERMINAL_VELOCITY) {
			fallingFast = true;
			fallingSlow = false;
		} else if (vy > 300) {
			fallingSlow = true;
		}

		if (isJumping && animTree != null) {
			animTree.setProperty("parameters/jump/request", 1); // ONE_SHOT_REQUEST_FIRE
		}

		boolean onFloor = (boolean) isOnFloor();
		if (onFloor) {
			if (fallingFast && animTree != null) {
				animTree.setProperty("parameters/land_hard/request", 1);
				noMoveHorizontalTime = 0.4;
			} else if (fallingSlow && animTree != null) {
				animTree.setProperty("parameters/land/request", 1);
			}

			vel = (Vector2) getProperty("velocity");
			vx = vel.getX();
			if (animTree != null) {
				if (Math.abs(vx) > 50) {
					animTree.setProperty("parameters/state/transition_request", STATE_RUN);
					animTree.setProperty("parameters/run_timescale/scale", Math.abs(vx) / 60.0);
				} else if (!isZeroApprox(vx)) {
					animTree.setProperty("parameters/state/transition_request", STATE_WALK);
					animTree.setProperty("parameters/walk_timescale/scale", Math.abs(vx) / 12.0);
				} else {
					animTree.setProperty("parameters/state/transition_request", STATE_IDLE);
				}
			}
			fallingFast = false;
			fallingSlow = false;
		} else {
			if (animTree != null) {
				vel = (Vector2) getProperty("velocity");
				if (vel.getY() > 0) {
					animTree.setProperty("parameters/state/transition_request", STATE_FALL);
				} else {
					animTree.setProperty("parameters/state/transition_request", STATE_FLY);
				}
			}
		}
	}

	private boolean tryJump() {
		boolean onFloor = (boolean) isOnFloor();
		if (onFloor) {
			Vector2 vel = (Vector2) getProperty("velocity");
			setProperty("velocity", new Vector2(vel.getX(), JUMP_VELOCITY));
			return true;
		}
		return false;
	}

	private static double moveToward(double current, double target, double maxDelta) {
		if (Math.abs(target - current) <= maxDelta) return target;
		return current + Math.signum(target - current) * maxDelta;
	}

	private static boolean isZeroApprox(double v) {
		return Math.abs(v) < 0.00001;
	}
}
