package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.node.Node;
import org.godot.math.Vector2;
import org.godot.singleton.Input;

@GodotClass(name = "FSMotion", parent = "Node")
public class FSMotion extends FSState {

    @Override
    public boolean handleInput(Object inputEvent) {
        org.godot.node.InputEvent event = inputEvent instanceof org.godot.node.InputEvent ? (org.godot.node.InputEvent) inputEvent : null;
        if (event != null) {
            if (event.isActionPressed("simulate_damage", false, false)) {
                org.godot.Godot sm = getParent();
                if (sm instanceof FSStateMachine stateMachine) {
                    stateMachine.pushState(STAGGER);
                }
            }
        }
        return false;
    }

    protected Vector2 getInputDirection() {
        Input input = Input.singleton();
        if (input == null) return Vector2.ZERO;
        Vector2 result = input.getVector("move_left", "move_right", "move_up", "move_down", 0.5);
        return result != null ? result : Vector2.ZERO;
    }

    protected void updateLookDirection(Vector2 direction) {
        if (direction.x == 0 && direction.y == 0) return;
        org.godot.Godot owner = (org.godot.Godot) getProperty("owner");
        if (owner != null) {
            Object lookDir = owner.getProperty("look_direction");
            Vector2 current = lookDir instanceof Vector2 ? (Vector2) lookDir : Vector2.ZERO;
            if (current.x != direction.x || current.y != direction.y) {
                if (owner instanceof FSPlayerController playerController) {
                    playerController.setLookDirection(direction);
                }
            }
        }
    }
}
