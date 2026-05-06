package demos.loading.serialization;

import org.godot.annotation.GodotClass;
import org.godot.Godot;
import org.godot.node.Node2D;
import org.godot.node.Node;

/**
 * Enemy that moves right across the screen and damages the player on contact.
 */
@GodotClass(name = "Enemy", parent = "Node2D")
public class Enemy extends Node2D {

    private static final double MOVEMENT_SPEED = 75.0;
    private static final double DAMAGE_PER_SECOND = 15.0;

    private Godot attacking = null;

    @Override
    public void _process(double delta) {
        // Damage the player if in range.
        if (attacking != null) {
            boolean valid = (boolean) call("is_instance_valid", attacking);
            if (valid) {
                double health = ((Number) attacking.getProperty("health")).doubleValue();
                health -= delta * DAMAGE_PER_SECOND;
                attacking.setProperty("health", health);
            }
        }

        // Move right.
        double posX = ((Number) getProperty("position")).doubleValue();
        posX += MOVEMENT_SPEED * delta;
        setProperty("position", posX);

        // Wrap around when going off-screen.
        if (posX >= 732) {
            setProperty("position", -32);
        }
    }

    /** Called when a body enters the AttackArea. */
    public void _onAttackAreaBodyEntered(Godot body) {
        boolean isPlayer = (boolean) body.call("is_class", "SerPlayer");
        if (isPlayer) {
            attacking = body;
        }
    }

    /** Called when a body exits the AttackArea. */
    public void _onAttackAreaBodyExited(Godot body) {
        attacking = null;
    }
}
