package demos.loading.serialization;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;
import org.godot.node.InputEvent;
import org.godot.node.SceneTree;
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

        Vector2 vec = input.getVector("move_left", "move_right", "move_up", "move_down");
        double lengthSq = vec.x * vec.x + vec.y * vec.y;
        if (lengthSq > 1.0) {
            double len = Math.sqrt(lengthSq);
            vec = new Vector2(vec.x / len, vec.y / len);
        }
        setVelocity(new Vector2(vec.x * MOVEMENT_SPEED, vec.y * MOVEMENT_SPEED));
        moveAndSlide();
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (sprite == null) return false;
        if (!(inputEvent instanceof InputEvent event)) return false;

        String[] actions = {"move_left", "move_right", "move_up", "move_down"};
        double[] rotations = {Math.PI / 2, -Math.PI / 2, Math.PI, 0.0};

        for (int i = 0; i < actions.length; i++) {
            if (event.isActionPressed(actions[i])) {
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
            SceneTree tree = getTree();
            if (tree != null) tree.reloadCurrentScene();
        }
    }

    public Godot getSprite() {
        return sprite;
    }
}
