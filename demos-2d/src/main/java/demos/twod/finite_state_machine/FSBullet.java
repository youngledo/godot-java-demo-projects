package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.node.CharacterBody2D;
import org.godot.math.Vector2;
import org.godot.node.SceneTree;
import org.godot.node.Viewport;


@GodotClass(name = "FSBullet", parent = "CharacterBody2D")
public class FSBullet extends CharacterBody2D {

    private Vector2 direction = Vector2.ZERO;
    private double speed = 1000.0;
    private Viewport root;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        setAsTopLevel(true);

        org.godot.node.SceneTree tree = getTree();
        if (tree != null && tree.getRoot() instanceof Viewport viewport) root = viewport;

        Object speedObj = getProperty("speed");
        if (speedObj instanceof Number) speed = ((Number) speedObj).doubleValue();
    }

    @Override
    public void _physicsProcess(double delta) {
        if (root != null) {
            org.godot.math.Rect2 rect = root.getVisibleRect();
            Object pos = getProperty("position");
            if (rect != null && pos instanceof Vector2) {
                Vector2 p = (Vector2) pos;
                boolean hasPoint = p.getX() >= rect.position.x && p.getX() <= rect.position.x + rect.size.x
                        && p.getY() >= rect.position.y && p.getY() <= rect.position.y + rect.size.y;
                if (!hasPoint) {
                    queueFree();
                    return;
                }
            }
        }

        Vector2 motion = direction.mul(speed * delta);
        Object collisionInfo = moveAndCollide(motion);
        if (collisionInfo != null) {
            queueFree();
        }
    }
}
