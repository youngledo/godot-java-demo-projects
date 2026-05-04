package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.CharacterBody2D;
import org.godot.math.Vector2;

@GodotClass(name = "FSPlayerController", parent = "CharacterBody2D")
public class FSPlayerController extends CharacterBody2D {

    private Vector2 lookDirection = new Vector2(1, 0);
    private boolean initialized = false;

    @Signal
    public void direction_changed() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    public void setLookDirection(Vector2 value) {
        lookDirection = value;
        call("emit_signal", "direction_changed", value);
    }

    public Vector2 getLookDirection() { return lookDirection; }

    @GodotMethod
    public void take_damage(Object attacker, double amount, Object effect) {
        org.godot.Godot atk = (org.godot.Godot) attacker;
        if (atk != null && (boolean) call("is_ancestor_of", atk)) return;

        org.godot.Godot stagger = (org.godot.Godot) call("get_node", "States/Stagger");
        if (stagger != null) {
            Object selfPos = getProperty("global_position");
            Object atkPos = atk != null ? atk.getProperty("global_position") : null;
            if (selfPos instanceof Vector2 && atkPos instanceof Vector2) {
                Vector2 knockDir = ((Vector2) atkPos).sub((Vector2) selfPos).normalized();
                stagger.setProperty("knockback_direction", knockDir);
            }
        }

        org.godot.Godot health = (org.godot.Godot) call("get_node", "Health");
        if (health != null) health.call("take_damage", amount, effect);
    }

    @GodotMethod
    public void set_dead(boolean value) {
        call("set_process_input", !value);
        call("set_physics_process", !value);
        org.godot.Godot collision = (org.godot.Godot) call("get_node", "CollisionPolygon2D");
        if (collision != null) collision.setProperty("disabled", value);
    }
}
