package demos.viewport.split_screen_input;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;

@GodotClass(name = "SSPlayer", parent = "CharacterBody2D")
public class SSPlayer extends CharacterBody2D {

    private static final double FACTOR = 200.0;

    private Vector2 movement = new Vector2(0, 0);
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    @Override
    public boolean _unhandledInput(Object inputEvent) {
        if (inputEvent instanceof org.godot.node.InputEvent) {
            org.godot.node.InputEvent evt = (org.godot.node.InputEvent) inputEvent;

            if ((boolean) evt.call("is_action_pressed", "ux_up", false) ||
                (boolean) evt.call("is_action_released", "ux_down", false)) {
                movement = new Vector2(movement.getX(), movement.getY() - 1);
                markInputHandled();
            } else if ((boolean) evt.call("is_action_pressed", "ux_down", false) ||
                       (boolean) evt.call("is_action_released", "ux_up", false)) {
                movement = new Vector2(movement.getX(), movement.getY() + 1);
                markInputHandled();
            } else if ((boolean) evt.call("is_action_pressed", "ux_left", false) ||
                       (boolean) evt.call("is_action_released", "ux_right", false)) {
                movement = new Vector2(movement.getX() - 1, movement.getY());
                markInputHandled();
            } else if ((boolean) evt.call("is_action_pressed", "ux_right", false) ||
                       (boolean) evt.call("is_action_released", "ux_left", false)) {
                movement = new Vector2(movement.getX() + 1, movement.getY());
                markInputHandled();
            }
        }
        return false;
    }

    @Override
    public void _physicsProcess(double delta) {
        Vector2 motion = new Vector2(movement.getX() * FACTOR * delta, movement.getY() * FACTOR * delta);
        moveAndCollide(motion);
    }

    private void markInputHandled() {
        Object vp = getViewport();
        if (vp != null) {
            ((org.godot.Godot) vp).call("set_input_as_handled");
        }
    }
}
