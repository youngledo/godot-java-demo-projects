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
            Object pressed = event.call("is_action_pressed", "simulate_damage");
            if (pressed instanceof Boolean && (Boolean) pressed) {
                org.godot.Godot sm = getParent();
                if (sm instanceof FSStateMachine) {
                    ((FSStateMachine) sm).pushState(STAGGER);
                } else {
                    sm.call("push_state", STAGGER);
                }
            }
        }
        return false;
    }

    protected Vector2 getInputDirection() {
        Input input = Input.singleton();
        if (input == null) return Vector2.ZERO;
        Object result = input.call("get_vector", "move_left", "move_right", "move_up", "move_down");
        return result instanceof Vector2 ? (Vector2) result : Vector2.ZERO;
    }

    protected void updateLookDirection(Vector2 direction) {
        if (direction.x == 0 && direction.y == 0) return;
        org.godot.Godot owner = (org.godot.Godot) getProperty("owner");
        if (owner != null) {
            Object lookDir = owner.getProperty("look_direction");
            Vector2 current = lookDir instanceof Vector2 ? (Vector2) lookDir : Vector2.ZERO;
            if (current.x != direction.x || current.y != direction.y) {
                owner.call("set_look_direction", direction);
            }
        }
    }
}
