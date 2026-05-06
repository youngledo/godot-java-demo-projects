package demos.twod.physics_platformer;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.RigidBody2D;
import org.godot.node.Node;

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
		org.godot.node.RigidBody2D bodyState = (org.godot.node.RigidBody2D) stateObj;
		if (bodyState == null) return;

		Vector2 vel = (Vector2) bodyState.getLinearVelocity();
		if (vel == null) vel = new Vector2(0, 0);

		String newAnim = anim;

		if (this.state == 1) {
			newAnim = "explode";
		} else if (this.state == 0) {
			newAnim = "walk";

			double wallSide = 0;
			long contactCount = (long) bodyState.getContactCount();

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
			org.godot.node.RayCast2D rcLeft = (org.godot.node.RayCast2D) call("get_node_or_null", "RaycastLeft");
			org.godot.node.RayCast2D rcRight = (org.godot.node.RayCast2D) call("get_node_or_null", "RaycastRight");

			boolean leftColliding = rcLeft != null && (boolean) rcLeft.isColliding();
			boolean rightColliding = rcRight != null && (boolean) rcRight.isColliding();

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
			org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
			if (animPlayer != null) animPlayer.play(anim);
		}

		bodyState.setLinearVelocity(vel);
	}

	private void flipSprite() {
		org.godot.node.Sprite2D sprite = (org.godot.node.Sprite2D) getNode("Sprite2D");
		if (sprite != null) {
			org.godot.math.Vector2 scale = (org.godot.math.Vector2) sprite.getProperty("scale");
			if (scale != null) {
				sprite.setProperty("scale", new Vector2(-direction, scale.getY()));
			}
		}
	}

	@GodotMethod
	public void BulletCollider(Object colliderObj, Object stateObj, Object normalObj) {
		if (state == 1) return;
		state = 1;

		org.godot.Godot collider = (org.godot.Godot) colliderObj;
		if (collider != null) {
			collider.call("disable");
		}

		org.godot.node.AudioStreamPlayer soundHit = (org.godot.node.AudioStreamPlayer) getNode("SoundHit");
		if (soundHit != null) soundHit.play();
	}

	@Override
	public void _exitTree() {
		org.godot.node.AudioStreamPlayer soundHit = (org.godot.node.AudioStreamPlayer) call("get_node_or_null", "SoundHit");
		if (soundHit != null) soundHit.stop();
		org.godot.node.AudioStreamPlayer soundExplode = (org.godot.node.AudioStreamPlayer) call("get_node_or_null", "SoundExplode");
		if (soundExplode != null) soundExplode.stop();
	}

	@GodotMethod
	public void _die() {
		queueFree();
	}

	@GodotMethod
	public void PreExplode() {
		org.godot.node.Node s1 = getNode("Shape1");
		org.godot.node.Node s2 = getNode("Shape2");
		org.godot.node.Node s3 = getNode("Shape3");
		if (s1 != null) s1.queueFree();
		if (s2 != null) s2.queueFree();
		if (s3 != null) s3.queueFree();

		org.godot.node.AudioStreamPlayer soundExplode = (org.godot.node.AudioStreamPlayer) getNode("SoundExplode");
		if (soundExplode != null) soundExplode.play();
	}
}
