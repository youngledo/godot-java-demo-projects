package demos.viewport.split_screen_input;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;
import org.godot.node.InputEvent;
import org.godot.node.Viewport;

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
        if (inputEvent instanceof InputEvent evt) {
            if (evt.isActionPressed("ux_up", false) || evt.isActionReleased("ux_down", false)) {
                movement = new Vector2(movement.getX(), movement.getY() - 1);
                markInputHandled();
            } else if (evt.isActionPressed("ux_down", false) || evt.isActionReleased("ux_up", false)) {
                movement = new Vector2(movement.getX(), movement.getY() + 1);
                markInputHandled();
            } else if (evt.isActionPressed("ux_left", false) || evt.isActionReleased("ux_right", false)) {
                movement = new Vector2(movement.getX() - 1, movement.getY());
                markInputHandled();
            } else if (evt.isActionPressed("ux_right", false) || evt.isActionReleased("ux_left", false)) {
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
        Viewport vp = getViewport();
        if (vp != null) {
            vp.setInputAsHandled();
        }
    }
}
