package demos.twod.physics_platformer;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.RigidBody2D;

@GodotClass(name = "PPEnemy", parent = "RigidBody2D")
public class PPEnemy extends RigidBody2D {

	private static final double WALK_SPEED = 50.0;

	private int state = 0; // 0=WALKING, 1=DYING
	private int direction = -1;
	private String anim = "";
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;
	}

	@Override
	public void _integrateForces(java.lang.Object stateObj) {
		org.godot.Godot bodyState = (org.godot.Godot) stateObj;
		if (bodyState == null) return;

		Vector2 vel = (Vector2) bodyState.call("get_linear_velocity");
		if (vel == null) vel = new Vector2(0, 0);

		String newAnim = anim;

		if (this.state == 1) {
			newAnim = "explode";
		} else if (this.state == 0) {
			newAnim = "walk";

			double wallSide = 0;
			long contactCount = (long) bodyState.call("get_contact_count");

			for (long i = 0; i < contactCount; i++) {
				Object collider = bodyState.call("get_contact_collider_object", i);
				Vector2 collisionNormal = (Vector2) bodyState.call("get_contact_local_normal", i);

				if (collider != null && collisionNormal != null) {
					if (collisionNormal.getX() > 0.9) {
						wallSide = 1.0;
					} else if (collisionNormal.getX() < -0.9) {
						wallSide = -1.0;
					}
				}
			}

			if (wallSide != 0 && wallSide != direction) {
				direction = -direction;
				flipSprite();
			}

			// Check raycasts for edge detection
			org.godot.Godot rcLeft = (org.godot.Godot) call("get_node_or_null", "RaycastLeft");
			org.godot.Godot rcRight = (org.godot.Godot) call("get_node_or_null", "RaycastRight");

			boolean leftColliding = rcLeft != null && (boolean) rcLeft.call("is_colliding");
			boolean rightColliding = rcRight != null && (boolean) rcRight.call("is_colliding");

			if (direction < 0 && !leftColliding && rightColliding) {
				direction = -direction;
				flipSprite();
			} else if (direction > 0 && !rightColliding && leftColliding) {
				direction = -direction;
				flipSprite();
			}

			vel = new Vector2(direction * WALK_SPEED, vel.getY());
		}

		if (!anim.equals(newAnim)) {
			anim = newAnim;
			org.godot.Godot animPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
			if (animPlayer != null) animPlayer.call("play", anim);
		}

		bodyState.call("set_linear_velocity", vel);
	}

	private void flipSprite() {
		org.godot.Godot sprite = (org.godot.Godot) call("get_node", "Sprite2D");
		if (sprite != null) {
			org.godot.math.Vector2 scale = (org.godot.math.Vector2) sprite.getProperty("scale");
			if (scale != null) {
				sprite.setProperty("scale", new Vector2(-direction, scale.getY()));
			}
		}
	}

	@GodotMethod
	public void _bullet_collider(Object colliderObj, Object stateObj, Object normalObj) {
		if (state == 1) return;
		state = 1;

		org.godot.Godot collider = (org.godot.Godot) colliderObj;
		if (collider != null) {
			collider.call("disable");
		}

		org.godot.Godot soundHit = (org.godot.Godot) call("get_node", "SoundHit");
		if (soundHit != null) soundHit.call("play");
	}

	@Override
	public void _exitTree() {
		org.godot.Godot soundHit = (org.godot.Godot) call("get_node_or_null", "SoundHit");
		if (soundHit != null) soundHit.call("stop");
		org.godot.Godot soundExplode = (org.godot.Godot) call("get_node_or_null", "SoundExplode");
		if (soundExplode != null) soundExplode.call("stop");
	}

	@GodotMethod
	public void _die() {
		call("queue_free");
	}

	@GodotMethod
	public void _pre_explode() {
		org.godot.Godot s1 = (org.godot.Godot) call("get_node", "Shape1");
		org.godot.Godot s2 = (org.godot.Godot) call("get_node", "Shape2");
		org.godot.Godot s3 = (org.godot.Godot) call("get_node", "Shape3");
		if (s1 != null) s1.call("queue_free");
		if (s2 != null) s2.call("queue_free");
		if (s3 != null) s3.call("queue_free");

		org.godot.Godot soundExplode = (org.godot.Godot) call("get_node", "SoundExplode");
		if (soundExplode != null) soundExplode.call("play");
	}
}
