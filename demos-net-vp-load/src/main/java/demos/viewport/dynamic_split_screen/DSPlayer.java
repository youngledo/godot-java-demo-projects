package demos.viewport.dynamic_split_screen;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;
import org.godot.singleton.Input;

@GodotClass(name = "DSPlayer", parent = "CharacterBody3D")
public class DSPlayer extends CharacterBody3D {

    @Export
    public int playerId = 1;

    @Export
    public double walkSpeed = 2.0;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    @Override
    public void _physicsProcess(double delta) {
        Input input = Input.singleton();

        String idStr = String.valueOf(playerId);
        double moveX = (double) input.call("get_vector",
            "move_left_player" + idStr,
            "move_right_player" + idStr,
            "move_up_player" + idStr,
            "move_down_player" + idStr
        );

        // get_vector returns a single value for the horizontal axis when called this way
        // In GDScript it's get_vector(left, right, up, down) which returns Vector2
        // We need to call it properly
        Object moveDirObj = input.call("get_vector",
            "move_left_player" + idStr,
            "move_right_player" + idStr,
            "move_up_player" + idStr,
            "move_down_player" + idStr
        );

        org.godot.math.Vector2 moveDir;
        if (moveDirObj instanceof org.godot.math.Vector2) {
            moveDir = (org.godot.math.Vector2) moveDirObj;
        } else {
            moveDir = new org.godot.math.Vector2(0, 0);
        }

        Vector3 vel = (Vector3) getProperty("velocity");
        if (vel == null) vel = new Vector3(0, 0, 0);

        vel = new Vector3(
            vel.getX() + moveDir.getX() * walkSpeed,
            vel.getY(),
            vel.getZ() + moveDir.getY() * walkSpeed
        );

        // Apply friction
        vel = new Vector3(vel.getX() * 0.9, vel.getY(), vel.getZ() * 0.9);

        setProperty("velocity", vel);
        call("move_and_slide");
    }
}
