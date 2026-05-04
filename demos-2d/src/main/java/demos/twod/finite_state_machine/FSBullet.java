package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.node.CharacterBody2D;
import org.godot.math.Vector2;


@GodotClass(name = "FSBullet", parent = "CharacterBody2D")
public class FSBullet extends CharacterBody2D {

    private Vector2 direction = Vector2.ZERO;
    private double speed = 1000.0;
    private org.godot.Godot root;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        call("set_as_top_level", true);

        org.godot.Godot tree = (org.godot.Godot) call("get_tree");
        if (tree != null) root = (org.godot.Godot) tree.call("get_root");

        Object speedObj = getProperty("speed");
        if (speedObj instanceof Number) speed = ((Number) speedObj).doubleValue();
    }

    @Override
    public void _physicsProcess(double delta) {
        if (root != null) {
            Object rect = root.call("get_visible_rect");
            Object pos = getProperty("position");
            if (rect instanceof org.godot.Godot && pos instanceof Vector2) {
                boolean hasPoint = (boolean) ((org.godot.Godot) rect).call("has_point", pos);
                if (!hasPoint) {
                    call("queue_free");
                    return;
                }
            }
        }

        Vector2 motion = direction.mul(speed * delta);
        Object collisionInfo = call("move_and_collide", motion);
        if (collisionInfo != null) {
            call("queue_free");
        }
    }
}
