package demos.loading.serialization;

import org.godot.annotation.GodotClass;
import org.godot.node.CharacterBody2D;
import org.godot.Godot;
import org.godot.math.Vector2;
import org.godot.singleton.Input;

/**
 * Player controlled with WASD/arrow keys. Has health that decreases when
 * enemies are nearby. Saves position, health, and rotation for serialization.
 */
@GodotClass(name = "SerPlayer", parent = "CharacterBody2D")
public class SerPlayer extends CharacterBody2D {

    private static final double MOVEMENT_SPEED = 240.0;

    private double health = 100.0;
    private Godot progressBar;
    private Godot sprite;
    private Input input;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        input = Input.singleton();
        progressBar = (Godot) getNode("ProgressBar");
        sprite = (Godot) getNode("Sprite2D");
    }

    @Override
    public void _process(double delta) {
        if (input == null) return;

        // Get movement input.
        Object moveVec = input.call("get_vector", "move_left", "move_right", "move_up", "move_down");
        if (moveVec instanceof Vector2) {
            Vector2 vec = (Vector2) moveVec;
            double lengthSq = vec.x * vec.x + vec.y * vec.y;
            if (lengthSq > 1.0) {
                double len = Math.sqrt(lengthSq);
                vec = new Vector2(vec.x / len, vec.y / len);
            }
            setProperty("velocity", new Vector2(vec.x * MOVEMENT_SPEED, vec.y * MOVEMENT_SPEED));
        }
        moveAndSlide();
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (sprite == null) return false;
        if (!(inputEvent instanceof Godot)) return false;
        Godot event = (Godot) inputEvent;

        String[] actions = {"move_left", "move_right", "move_up", "move_down"};
        double[] rotations = {Math.PI / 2, -Math.PI / 2, Math.PI, 0.0};

        for (int i = 0; i < actions.length; i++) {
            boolean pressed = (boolean) event.call("is_action_pressed", actions[i]);
            if (pressed) {
                sprite.setProperty("rotation", rotations[i]);
                break;
            }
        }
        return false;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double value) {
        this.health = value;
        if (progressBar != null) {
            progressBar.setProperty("value", value);
        }
        if (health <= 0.0) {
            // The player died - reload scene.
            Godot tree = (Godot) getTree();
            if (tree != null) tree.call("reload_current_scene");
        }
    }

    public Godot getSprite() {
        return sprite;
    }
}
