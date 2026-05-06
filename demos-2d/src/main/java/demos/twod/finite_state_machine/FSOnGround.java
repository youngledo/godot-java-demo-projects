package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;

@GodotClass(name = "FSOnGround", parent = "Node")
public class FSOnGround extends FSMotion {

    protected double speed = 0;
    protected Vector2 velocity = Vector2.ZERO;

    @Override
    public boolean handleInput(Object inputEvent) {
        org.godot.Godot event = (org.godot.Godot) inputEvent;
        if (event != null) {
            Object pressed = event.call("is_action_pressed", "jump");
            if (pressed instanceof Boolean && (Boolean) pressed) {
                org.godot.Godot sm = getParent();
                if (sm != null) sm.call("push_state", JUMP);
            }
        }
        return super.handleInput(inputEvent);
    }
}
