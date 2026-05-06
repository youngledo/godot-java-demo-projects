package demos.threed.platformer;

import org.godot.annotation.GodotClass;
import org.godot.node.CharacterBody3D;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.node.Node;
import org.godot.singleton.Input;

@GodotClass(name = "PLPlayer", parent = "CharacterBody3D")
public class PLPlayer extends CharacterBody3D {

	private static final double SHOOT_TIME = 1.5;
	private static final double MAX_SPEED = 6.0;
	private static final double TURN_SPEED = 40.0;
	private static final double JUMP_VELOCITY = 12.5;
	private static final double BULLET_SPEED = 20.0;
	private static final double ACCEL = 14.0;
	private static final double DEACCEL = 14.0;
	private static final double AIR_ACCEL_FACTOR = 0.5;
	private static final double SHARP_TURN_THRESHOLD = Math.toRadians(140.0);
	private static final Vector3 CHAR_SCALE = new Vector3(0.3, 0.3, 0.3);

	private static final int ANIM_FLOOR = 0;
	private static final int ANIM_AIR = 1;

	private Vector3 movementDir = new Vector3();
	private boolean jumping = false;
	private boolean prevShoot = false;
	private double shootBlend = 0.0;
	private int coins = 0;

	private Vector3 initialPosition;
	private Vector3 gravity;
	private org.godot.node.Node3D camera;
	private org.godot.node.Node animationTree;
	private org.godot.singleton.Input input;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		input = org.godot.singleton.Input.singleton();
		gravity = new Vector3(0, -9.8, 0);

		initialPosition = (Vector3) getProperty("position");

		org.godot.node.Node target = getNode("Target");
		if (target != null) {
			camera = (org.godot.node.Node3D) target.getNode("Camera3D");
		}
		animationTree = getNode("AnimationTree");
	}

	@Override
	public void _physicsProcess(double delta) {
		if (input == null) return;

		Vector3 pos = (Vector3) getProperty("global_position");
		if (pos != null && pos.y < -12) {
			setProperty("position", initialPosition);
			setProperty("velocity", Vector3.ZERO);
			resetPhysicsInterpolation();
		}

		if ((boolean) input.isActionPressed("reset_position")) {
			setProperty("position", initialPosition);
			setProperty("velocity", Vector3.ZERO);
			resetPhysicsInterpolation();
		}

		updateCoinLabels();

		Vector3 velocity = (Vector3) getProperty("velocity");
		if (velocity == null) velocity = new Vector3();
		velocity = velocity.add(gravity.mul(delta));

		int anim = ANIM_FLOOR;
		double verticalVelocity = velocity.y;
		Vector3 horizontalVelocity = new Vector3(velocity.x, 0, velocity.z);
		Vector3 horizontalDirection = horizontalVelocity.normalized();
		double horizontalSpeed = horizontalVelocity.length();

		Vector3 movementDirection = calculateMovementDirection();

		boolean jumpAttempt = (boolean) input.isActionPressed("jump");
		boolean onFloor = (boolean) isOnFloor();

		if (onFloor) {
			double dot = horizontalDirection.dot(movementDirection);
			double acos = horizontalSpeed > 0.1 ? Math.acos(Math.max(-1, Math.min(1, dot))) : 0;
			boolean sharpTurn = horizontalSpeed > 0.1 && acos > SHARP_TURN_THRESHOLD;

			if (movementDirection.length() > 0.1 && !sharpTurn) {
				if (horizontalSpeed > 0.001) {
					horizontalDirection = adjustFacing(horizontalDirection, movementDirection, delta,
							1.0 / horizontalSpeed * TURN_SPEED, Vector3.UP);
				} else {
					horizontalDirection = movementDirection;
				}
				if (horizontalSpeed < MAX_SPEED) {
					horizontalSpeed += ACCEL * delta;
				}
			} else {
				horizontalSpeed -= DEACCEL * delta;
				if (horizontalSpeed < 0) horizontalSpeed = 0;
			}

			horizontalVelocity = horizontalDirection.mul(horizontalSpeed);
			updateSkeletonFacing(movementDirection, horizontalSpeed, delta);

			if (!jumping && jumpAttempt) {
				verticalVelocity = JUMP_VELOCITY;
				jumping = true;
				org.godot.node.Node soundJump = getNode("SoundJump");
				if (soundJump != null) soundJump.call("play");
			}
		} else {
			anim = ANIM_AIR;
			if (movementDirection.length() > 0.1) {
				horizontalVelocity = horizontalVelocity.add(movementDirection.mul(ACCEL * AIR_ACCEL_FACTOR * delta));
				if (horizontalVelocity.length() > MAX_SPEED) {
					horizontalVelocity = horizontalVelocity.normalized().mul(MAX_SPEED);
				}
			}
			if ((boolean) input.isActionJustReleased("jump") && velocity.y > 0.0) {
				verticalVelocity *= 0.7;
			}
		}

		if (jumping && verticalVelocity < 0) jumping = false;

		velocity = horizontalVelocity.add(new Vector3(0, verticalVelocity, 0));
		if (onFloor) movementDir = velocity;
		setProperty("velocity", velocity);
		moveAndSlide();

		handleShooting();
		updateAnimationTree(anim, horizontalSpeed, velocity);

		prevShoot = checkShootPressed();
	}

	private void updateCoinLabels() {
		org.godot.node.Node coinCount = getNode("Player/Skeleton/CoinCount");
		if (coinCount == null) return;
		String coinStr = String.valueOf(coins);
		coinCount.setProperty("text", coinStr);
		String[] parallaxNames = {"Parallax", "Parallax2", "Parallax3", "Parallax4"};
		for (String name : parallaxNames) {
			org.godot.Godot p = (org.godot.Godot) coinCount.getNode(name);
			if (p != null) p.setProperty("text", coinStr);
		}
	}

	private Vector3 calculateMovementDirection() {
		Vector3 movementDirection = new Vector3();
		if (camera == null || input == null) return movementDirection;

		Object camBasisObj = camera.getGlobalTransform();
		if (!(camBasisObj instanceof Transform3D)) return movementDirection;

		Transform3D camXform = (Transform3D) camBasisObj;
		Basis camBasis = camXform.getBasis();

		Object moveVecObj = input.getVector("move_left", "move_right", "move_forward", "move_back");
		if (moveVecObj instanceof Vector2) {
			Vector2 moveVec2 = (Vector2) moveVecObj;
			Vector3 moveVec3 = new Vector3(moveVec2.x, 0, moveVec2.y);
			movementDirection = camBasis.apply(moveVec3);
			movementDirection.y = 0;
			movementDirection = movementDirection.normalized();
		}
		return movementDirection;
	}

	private void updateSkeletonFacing(Vector3 movementDirection, double horizontalSpeed, double delta) {
		org.godot.node.Node skeleton = getNode("Player/Skeleton");
		if (skeleton == null) return;

		Object meshXformObj = skeleton.call("get_transform");
		if (!(meshXformObj instanceof Transform3D)) return;

		Transform3D meshXform = (Transform3D) meshXformObj;
		Basis meshBasis = meshXform.getBasis();
		Vector3 facingMesh = new Vector3(-meshBasis.xx, -meshBasis.xy, -meshBasis.xz).normalized();
		facingMesh = facingMesh.sub(Vector3.UP.mul(facingMesh.dot(Vector3.UP))).normalized();

		if (horizontalSpeed > 0) {
			facingMesh = adjustFacing(facingMesh, movementDirection, delta,
					1.0 / horizontalSpeed * TURN_SPEED, Vector3.UP);
		}

		Vector3 col0 = facingMesh.mul(-1);
		Vector3 col2 = col0.cross(Vector3.UP).normalized();
		Vector3 scaledCol0 = col0.mul(CHAR_SCALE.x);
		Vector3 scaledCol1 = Vector3.UP.mul(CHAR_SCALE.y);
		Vector3 scaledCol2 = col2.mul(CHAR_SCALE.z);
		Basis m3 = new Basis(
				scaledCol0.x, scaledCol0.y, scaledCol0.z,
				scaledCol1.x, scaledCol1.y, scaledCol1.z,
				scaledCol2.x, scaledCol2.y, scaledCol2.z);
		skeleton.call("set_transform", new Transform3D(m3, meshXform.getOrigin()));
	}

	private void handleShooting() {
		if (shootBlend > 0) {
			shootBlend *= 0.97;
			if (shootBlend < 0) shootBlend = 0;
		}
		if (!checkShootPressed() || prevShoot) return;

		shootBlend = SHOOT_TIME;

		org.godot.node.PackedScene bulletSceneObj = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://player/bullet/bullet.tscn");
		if (bulletSceneObj == null) return;

		org.godot.node.RigidBody3D bullet = (org.godot.node.RigidBody3D) (org.godot.Godot) bulletSceneObj.instantiate();
		org.godot.node.Node bulletNode = getNode("Player/Skeleton/Bullet");
		if (bulletNode == null || bullet == null) return;

		Object btObj = bulletNode.call("get_global_transform");
		if (btObj instanceof Transform3D) {
			Transform3D bt = (Transform3D) btObj;
			bullet.setTransform(bt);

			org.godot.node.Node parent = (org.godot.node.Node) getParent();
			if (parent != null) parent.addChild((org.godot.node.Node) bullet);

			Basis btBasis = bt.getBasis();
			Vector3 bulletDir = new Vector3(btBasis.zx, btBasis.zy, btBasis.zz).normalized();
			bullet.setLinearVelocity(bulletDir.mul(BULLET_SPEED));
			bullet.addCollisionExceptionWith(this);
		}

		org.godot.node.Node soundShoot = getNode("SoundShoot");
		if (soundShoot != null) soundShoot.call("play");
	}

	private boolean checkShootPressed() {
		if (input == null) return false;
		return (boolean) input.isActionPressed("shoot");
	}

	private void updateAnimationTree(int anim, double horizontalSpeed, Vector3 velocity) {
		if (animationTree == null) return;

		boolean onFloor = (boolean) isOnFloor();
		if (onFloor) {
			animationTree.set("parameters/run/blend_amount", horizontalSpeed / MAX_SPEED);
			animationTree.set("parameters/speed/blend_amount", Math.min(1.0, horizontalSpeed / (MAX_SPEED * 0.5)));
		}
		animationTree.set("parameters/state/blend_amount", (double) anim);
		double airDir = Math.max(0, Math.min(1, -velocity.y / 4.0 + 0.5));
		animationTree.set("parameters/air_dir/blend_amount", airDir);
		animationTree.set("parameters/gun/blend_amount", Math.min(shootBlend, 1.0));
	}

	private Vector3 adjustFacing(Vector3 facing, Vector3 target, double step, double adjustRate, Vector3 currentGn) {
		Vector3 normal = target;
		Vector3 t = normal.cross(currentGn).normalized();
		double x = normal.dot(facing);
		double y = t.dot(facing);
		double ang = Math.atan2(y, x);
		if (Math.abs(ang) < 0.001) return facing;
		double s = Math.signum(ang);
		ang = ang * s;
		double turn = ang * adjustRate * step;
		double a = ang < turn ? ang : turn;
		ang = (ang - a) * s;
		return normal.mul(Math.cos(ang)).add(t.mul(Math.sin(ang))).mul(facing.length());
	}
}
