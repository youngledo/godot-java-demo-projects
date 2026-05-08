package demos.threed.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.RigidBody3D;
import org.godot.math.Vector3;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.node.Node;

@GodotClass(name = "PLEnemy", parent = "RigidBody3D")
public class PLEnemy extends RigidBody3D {

	private static final double ACCEL = 5.0;
	private static final double DEACCEL = 20.0;
	private static final double MAX_SPEED = 2.0;
	private static final double ROT_SPEED = 1.0;

	private boolean prevAdvance = false;
	private boolean dying = false;
	private double rotDir = 4.0;

	private Vector3 gravity = new Vector3(0, -9.8, 0);
	private org.godot.node.AnimationPlayer animationPlayer;
	private org.godot.node.Node rayFloor;
	private org.godot.node.Node rayWall;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
		if (ps != null) {
			Object gravVal = ps.call("get_setting", "physics/3d/default_gravity");
			Object gravVecVal = ps.call("get_setting", "physics/3d/default_gravity_vector");
			double gravMag = gravVal != null ? ((Number) gravVal).doubleValue() : 9.8;
			Vector3 gravDir = gravVecVal != null ? (Vector3) gravVecVal : new Vector3(0, -1, 0);
			gravity = gravDir.mul(gravMag);
		}

		animationPlayer = (org.godot.node.AnimationPlayer) getNode("Enemy/AnimationPlayer");
		rayFloor = getNode("Enemy/Skeleton/RayFloor");
		rayWall = getNode("Enemy/Skeleton/RayWall");
	}

	@Override
	public void _physicsProcess(double delta) {
		if (dying) return;

		Object linVelObj = call("get_linear_velocity");
		Vector3 linVelocity = linVelObj instanceof Vector3 ? (Vector3) linVelObj : new Vector3();

		Vector3 up = gravity.mul(-1).normalized();

		boolean advance = false;
		if (rayFloor != null && rayWall != null) {
			boolean floorColliding = (boolean) rayFloor.call("is_colliding");
			boolean wallColliding = (boolean) rayWall.call("is_colliding");
			advance = floorColliding && !wallColliding;
		}

		org.godot.node.Node skeleton = getNode("Enemy/Skeleton");
		Vector3 dir = Vector3.FORWARD;
		if (skeleton != null) {
			Object skelXform = skeleton.call("get_transform");
			if (skelXform instanceof Transform3D) {
				Basis skelBasis = ((Transform3D) skelXform).getBasis();
				dir = new Vector3(skelBasis.zx, skelBasis.zy, skelBasis.zz).normalized();
			}
		}
		Vector3 deaccelDir = dir;

		if (advance) {
			if (dir.dot(linVelocity) < MAX_SPEED) {
				applyCentralForce(dir.mul(ACCEL));
			}
			deaccelDir = dir.cross(gravity).normalized();
		} else {
			if (prevAdvance) rotDir = 1;
			Basis rotBasis = Basis.fromAxisAngle(up, rotDir * ROT_SPEED * delta);
			dir = rotBasis.apply(dir);
			if (skeleton != null) {
				Vector3 fwd = dir.mul(-1);
				Vector3 col0 = fwd;
				Vector3 col1 = up;
				Vector3 col2 = col0.cross(col1).normalized();
				Transform3D lookXform = new Transform3D(col0, col1, col2, Vector3.ZERO);
				skeleton.call("set_transform", lookXform);
			}
		}

		double dspeed = deaccelDir.dot(linVelocity);
		dspeed -= DEACCEL * delta;
		if (dspeed < 0) dspeed = 0;

		linVelocity = linVelocity.sub(deaccelDir.mul(deaccelDir.dot(linVelocity))).add(deaccelDir.mul(dspeed));
		setLinearVelocity(linVelocity);

		prevAdvance = advance;
	}

	@GodotMethod
	public void destroy() {
		dying = true;
		setProperty("axis_lock_angular_x", false);
		setProperty("axis_lock_angular_y", false);
		setProperty("axis_lock_angular_z", false);
		setProperty("collision_layer", 0L);

		if (animationPlayer != null) {
			animationPlayer.play("impact");
			animationPlayer.queue("extra/explode");
		}
		org.godot.node.AudioStreamPlayer soundWalk = (org.godot.node.AudioStreamPlayer) getNode("SoundWalkLoop");
		if (soundWalk != null) soundWalk.stop();
		org.godot.node.Node soundHit = getNode("SoundHit");
		if (soundHit != null) soundHit.call("play");
	}
}
