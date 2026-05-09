package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.CharacterBody2D;
import org.godot.math.Vector2;
import org.godot.node.Node;
import org.godot.node.Node2D;

@GodotClass(name = "FSPlayerController", parent = "CharacterBody2D")
public class FSPlayerController extends CharacterBody2D {

    private Vector2 lookDirection = new Vector2(1, 0);
    private boolean initialized = false;

    @Signal
    public void directionChanged() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    public void setLookDirection(Vector2 value) {
        lookDirection = value;
        emitSignal("direction_changed", value);
    }

    public Vector2 getLookDirection() { return lookDirection; }

    @GodotMethod
    public void takeDamage(Object attacker, double amount, Object effect) {
        if (attacker instanceof Node attackerNode && isAncestorOf(attackerNode)) return;

        FSStagger stagger = getNodeAs("States/Stagger", FSStagger.class);
        if (stagger != null && attacker instanceof Node2D attackerNode) {
            Vector2 knockDir = attackerNode.getGlobalPosition().sub(getGlobalPosition()).normalized();
            stagger.setProperty("knockback_direction", knockDir);
        }

        FSHealth health = getNodeAs("Health", FSHealth.class);
        if (health != null) health.takeDamage(amount, effect);
    }

    @GodotMethod
    public void setDead(boolean value) {
        setProcessInput(!value);
        setPhysicsProcess(!value);
        org.godot.node.CollisionPolygon2D collision = (org.godot.node.CollisionPolygon2D) getNode("CollisionPolygon2D");
        if (collision != null) collision.setProperty("disabled", value);
    }
}
