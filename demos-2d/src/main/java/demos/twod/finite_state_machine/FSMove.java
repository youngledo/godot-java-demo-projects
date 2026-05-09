package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.singleton.Input;

@GodotClass(name = "FSMove", parent = "Node")
public class FSMove extends FSOnGround {

    private static final double MAX_WALK_SPEED = 450.0;
    private static final double MAX_RUN_SPEED = 700.0;

    @Override
    public void enter() {
        speed = 0;
        velocity = Vector2.ZERO;
        Vector2 inputDir = getInputDirection();
        updateLookDirection(inputDir);

        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) owner.getNode("AnimationPlayer");
            if (animPlayer != null) animPlayer.play(WALK);
        }
    }

    @Override
    public void update(double delta) {
        Vector2 inputDir = getInputDirection();
        if (inputDir.x == 0 && inputDir.y == 0) {
            FSStateMachine sm = (FSStateMachine) getParent();
            if (sm != null) sm.changeState(IDLE);
            return;
        }
        updateLookDirection(inputDir);

        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
        boolean running = input != null && (boolean) input.isActionPressed("run");
        speed = running ? MAX_RUN_SPEED : MAX_WALK_SPEED;

        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            Vector2 vel = inputDir.normalized().mul(speed);
            owner.setProperty("velocity", vel);
            ((org.godot.node.CharacterBody2D) owner).moveAndSlide();

            int collisionCount = owner instanceof org.godot.node.CharacterBody2D body ? body.getSlideCollisionCount() : 0;
            if (collisionCount > 0) {
                // Collision handling
            }
        }
    }
}
