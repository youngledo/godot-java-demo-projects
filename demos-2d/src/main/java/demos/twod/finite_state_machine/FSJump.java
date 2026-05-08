package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;

@GodotClass(name = "FSJump", parent = "Node")
public class FSJump extends FSMotion {

    private static final double BASE_MAX_HORIZONTAL_SPEED = 400.0;
    private static final double AIR_ACCELERATION = 1000.0;
    private static final double AIR_DECELERATION = 2000.0;
    private static final double AIR_STEERING_POWER = 50.0;
    private static final double GRAVITY = 1600.0;

    private Vector2 enterVelocity = Vector2.ZERO;
    private double maxHorizontalSpeed = 0;
    private double horizontalSpeed = 0;
    private Vector2 horizontalVelocity = Vector2.ZERO;
    private double verticalSpeed = 0;
    private double height = 0;

    public void initialize(double spd, Vector2 vel) {
        horizontalSpeed = spd;
        maxHorizontalSpeed = spd > 0 ? spd : BASE_MAX_HORIZONTAL_SPEED;
        enterVelocity = vel;
    }

    @Override
    public void enter() {
        Vector2 inputDir = getInputDirection();
        updateLookDirection(inputDir);
        horizontalVelocity = (inputDir.x != 0 || inputDir.y != 0) ? enterVelocity : Vector2.ZERO;
        verticalSpeed = 600.0;

        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) owner.getNode("AnimationPlayer");
            if (animPlayer != null) animPlayer.play(IDLE);
        }
    }

    @Override
    public void update(double delta) {
        Vector2 inputDir = getInputDirection();
        updateLookDirection(inputDir);
        moveHorizontally(delta, inputDir);
        animateJumpHeight(delta);

        if (height <= 0) {
            FSStateMachine sm = (FSStateMachine) getParent();
            if (sm != null) sm.changeState(PREVIOUS);
        }
    }

    private void moveHorizontally(double delta, Vector2 direction) {
        if (direction.x != 0 || direction.y != 0) {
            horizontalSpeed += AIR_ACCELERATION * delta;
        } else {
            horizontalSpeed -= AIR_DECELERATION * delta;
        }
        horizontalSpeed = Math.max(0, Math.min(horizontalSpeed, maxHorizontalSpeed));

        Vector2 targetVelocity = direction.normalized().mul(horizontalSpeed);
        Vector2 steering = targetVelocity.sub(horizontalVelocity).normalized().mul(AIR_STEERING_POWER);
        horizontalVelocity = horizontalVelocity.add(steering);

        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            owner.setProperty("velocity", horizontalVelocity);
            owner.call("move_and_slide");
        }
    }

    private void animateJumpHeight(double delta) {
        verticalSpeed -= GRAVITY * delta;
        height += verticalSpeed * delta;
        height = Math.max(0, height);

        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            org.godot.node.Node bodyPivot = (org.godot.node.Node) owner.getNode("BodyPivot");
            if (bodyPivot != null) {
                Object pos = bodyPivot.getProperty("position");
                Vector2 p = pos instanceof Vector2 ? (Vector2) pos : Vector2.ZERO;
                bodyPivot.setProperty("position", new Vector2(p.x, -height));
            }
        }
    }
}
