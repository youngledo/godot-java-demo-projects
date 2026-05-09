package demos.loading.serialization;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.Node;
import org.godot.node.Node2D;

/**
 * Enemy that moves right across the screen and damages the player on contact.
 */
@GodotClass(name = "Enemy", parent = "Node2D")
public class Enemy extends Node2D {

    private static final double MOVEMENT_SPEED = 75.0;
    private static final double DAMAGE_PER_SECOND = 15.0;

    private SerPlayer attacking = null;

    @Override
    public void _process(double delta) {
        if (attacking != null && isInstanceValid(attacking)) {
            double health = attacking.getHealth();
            health -= delta * DAMAGE_PER_SECOND;
            attacking.setHealth(health);
        }

        Vector2 position = getPosition();
        double posX = position.getX() + MOVEMENT_SPEED * delta;
        setPosition(new Vector2(posX, position.getY()));

        if (posX >= 732) {
            setPosition(new Vector2(-32, position.getY()));
        }
    }

    public void _onAttackAreaBodyEntered(Node body) {
        if (body instanceof SerPlayer player) {
            attacking = player;
        }
    }

    public void _onAttackAreaBodyExited(Node body) {
        attacking = null;
    }
}
