package demos.viewport.dynamic_split_screen;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
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
        Vector2 moveDir = input.getVector(
            "move_left_player" + idStr,
            "move_right_player" + idStr,
            "move_up_player" + idStr,
            "move_down_player" + idStr
        );

        Vector3 vel = getVelocity();
        if (vel == null) vel = new Vector3(0, 0, 0);

        vel = new Vector3(
            vel.getX() + moveDir.getX() * walkSpeed,
            vel.getY(),
            vel.getZ() + moveDir.getY() * walkSpeed
        );

        vel = new Vector3(vel.getX() * 0.9, vel.getY(), vel.getZ() * 0.9);

        setVelocity(vel);
        moveAndSlide();
    }
}
