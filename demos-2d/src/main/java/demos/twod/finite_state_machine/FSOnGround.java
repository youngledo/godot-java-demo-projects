package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;

@GodotClass(name = "FSOnGround", parent = "Node")
public class FSOnGround extends FSMotion {

    protected double speed = 0;
    protected Vector2 velocity = Vector2.ZERO;

    @Override
    public boolean handleInput(Object inputEvent) {
        org.godot.node.InputEvent event = inputEvent instanceof org.godot.node.InputEvent ? (org.godot.node.InputEvent) inputEvent : null;
        if (event != null) {
            if (event.isActionPressed("jump", false, false)) {
                org.godot.Godot sm = getParent();
                if (sm instanceof FSStateMachine stateMachine) {
                    stateMachine.pushState(JUMP);
                }
            }
        }
        return super.handleInput(inputEvent);
    }
}
