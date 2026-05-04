package demos.twod.physics_platformer;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector2;
import org.godot.node.RigidBody2D;

@GodotClass(name = "PPPlayer", parent = "RigidBody2D")
public class PPPlayer extends RigidBody2D {

	private static final double WALK_ACCEL = 1000.0;
	private static final double WALK_DEACCEL = 1000.0;
	private static final double WALK_MAX_VELOCITY = 200.0;
	private static final double AIR_ACCEL = 250.0;
	private static final double AIR_DEACCEL = 250.0;
	private static final double JUMP_VELOCITY = 380.0;
	private static final double STOP_JUMP_FORCE = 450.0;
	private static final double MAX_FLOOR_AIRBORNE_TIME = 0.15;

	@Export
	public double engineForceValue = 40.0;

	private String anim = "";
	private boolean sidingLeft = false;
	private boolean jumping = false;
	private boolean stoppingJump = false;
	private double floorHVelocity = 0.0;
	private double airborneTime = 1e20;
	private double shootTime = 1e20;
	private boolean shooting = false;
	private org.godot.Godot sprite;
	private org.godot.Godot animationPlayer;
	private org.godot.Godot bulletShoot;
	private org.godot.Godot soundJump;
	private org.godot.Godot soundShoot;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		sprite = (org.godot.Godot) call("get_node", "Sprite2D");
		animationPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
		bulletShoot = (org.godot.Godot) call("get_node", "BulletShoot");
		soundJump = (org.godot.Godot) call("get_node", "SoundJump");
		soundShoot = (org.godot.Godot) call("get_node", "SoundShoot");
	}

	@Override
	public void _physicsProcess(double delta) {
		Vector2 vel = (Vector2) getProperty("linear_velocity");
		if (vel == null) vel = new Vector2(0, 0);

		String newAnim = anim;
		boolean newSidingLeft = sidingLeft;

		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
		boolean moveLeft = (boolean) input.call("is_action_pressed", "move_left");
		boolean moveRight = (boolean) input.call("is_action_pressed", "move_right");
		boolean jump = (boolean) input.call("is_action_pressed", "jump");
		boolean shoot = (boolean) input.call("is_action_pressed", "shoot");
		boolean spawn = (boolean) input.call("is_action_just_pressed", "spawn");

		vel = new Vector2(vel.getX() - floorHVelocity, vel.getY());
		floorHVelocity = 0.0;

		// Simplified floor detection for RigidBody2D:
		// Check contact count and if any contact has an upward-facing normal.
		boolean foundFloor = false;
		long contactCount = (long) call("get_contact_count");
		for (int ci = 0; ci < contactCount; ci++) {
			org.godot.math.Vector2 normal = (org.godot.math.Vector2) call("get_contact_local_normal", ci);
			if (normal != null && normal.dot(new Vector2(0, -1)) > 0.6) {
				foundFloor = true;
				break;
			}
		}

		if (foundFloor) {
			airborneTime = 0.0;
		} else {
			airborneTime += delta;
		}

		boolean onFloor = airborneTime < MAX_FLOOR_AIRBORNE_TIME;

		// Jump logic
		if (jumping) {
			if (vel.getY() > 0) {
				jumping = false;
			} else if (!jump) {
				stoppingJump = true;
			}
			if (stoppingJump) {
				vel = new Vector2(vel.getX(), vel.getY() + STOP_JUMP_FORCE * delta);
			}
		}

		double vx = vel.getX();

		if (onFloor) {
			if (moveLeft && !moveRight) {
				if (vx > -WALK_MAX_VELOCITY) vx -= WALK_ACCEL * delta;
			} else if (moveRight && !moveLeft) {
				if (vx < WALK_MAX_VELOCITY) vx += WALK_ACCEL * delta;
			} else {
				double xv = Math.abs(vx);
				xv -= WALK_DEACCEL * delta;
				if (xv < 0) xv = 0;
				vx = Math.signum(vx) * xv;
			}

			if (!jumping && jump) {
				vel = new Vector2(vx, -JUMP_VELOCITY);
				vx = vel.getX();
				jumping = true;
				stoppingJump = false;
				if (soundJump != null) soundJump.call("play");
			}

			if (vx < 0 && moveLeft) newSidingLeft = true;
			else if (vx > 0 && moveRight) newSidingLeft = false;

			if (jumping) newAnim = "jumping";
			else if (Math.abs(vx) < 0.1) newAnim = "idle";
			else newAnim = "run";
		} else {
			if (moveLeft && !moveRight) {
				if (vx > -WALK_MAX_VELOCITY) vx -= AIR_ACCEL * delta;
			} else if (moveRight && !moveLeft) {
				if (vx < WALK_MAX_VELOCITY) vx += AIR_ACCEL * delta;
			} else {
				double xv = Math.abs(vx);
				xv -= AIR_DEACCEL * delta;
				if (xv < 0) xv = 0;
				vx = Math.signum(vx) * xv;
			}

			if (vel.getY() < 0) newAnim = "jumping";
			else newAnim = "falling";
		}

		if (newSidingLeft != sidingLeft && sprite != null) {
			sprite.setProperty("scale", new Vector2(newSidingLeft ? -1 : 1, 1));
			sidingLeft = newSidingLeft;
		}

		if (!anim.equals(newAnim) && animationPlayer != null) {
			anim = newAnim;
			animationPlayer.call("play", anim);
		}

		shooting = shoot;

		setProperty("linear_velocity", new Vector2(vx + floorHVelocity, vel.getY()));
	}

	@Override
	public void _exitTree() {
		if (soundJump != null) soundJump.call("stop");
		if (soundShoot != null) soundShoot.call("stop");
		soundJump = null;
		soundShoot = null;
		sprite = null;
		animationPlayer = null;
		bulletShoot = null;
	}

	@GodotMethod
	public void _shot_bullet() {
		shootTime = 0;
		Object bulletSceneObj = org.godot.singleton.ResourceLoader.singleton().load("res://player/bullet.tscn", "", 1);
		if (bulletSceneObj == null) return;
		org.godot.Godot bullet = (org.godot.Godot) ((org.godot.Godot) bulletSceneObj).call("instantiate");

		Vector2 pos = (Vector2) getProperty("position");
		if (pos == null) pos = new Vector2(0, 0);

		Vector2 shootPos = bulletShoot != null ? (Vector2) bulletShoot.getProperty("position") : new Vector2(20, 0);
		if (shootPos == null) shootPos = new Vector2(20, 0);

		double speedScale = sidingLeft ? -1.0 : 1.0;

		bullet.setProperty("position", new Vector2(
			pos.getX() + shootPos.getX() * speedScale,
			pos.getY() + shootPos.getY()));

		org.godot.Godot parent = (org.godot.Godot) call("get_parent");
		if (parent != null) parent.call("add_child", bullet);

		bullet.setProperty("linear_velocity", new Vector2(400.0 * speedScale, -40));
		if (soundShoot != null) soundShoot.call("play");
	}
}
