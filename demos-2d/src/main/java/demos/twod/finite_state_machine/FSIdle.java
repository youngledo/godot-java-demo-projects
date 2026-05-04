package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;

@GodotClass(name = "FSIdle", parent = "Node")
public class FSIdle extends FSOnGround {

    @Override
    public void enter() {
        org.godot.Godot owner = (org.godot.Godot) getProperty("owner");
        if (owner != null) {
            org.godot.Godot animPlayer = (org.godot.Godot) owner.call("get_node", "AnimationPlayer");
            if (animPlayer != null) animPlayer.call("play", IDLE);
        }
    }

    @Override
    public void update(double delta) {
        Vector2 inputDir = getInputDirection();
        if (inputDir.x != 0 || inputDir.y != 0) {
            org.godot.Godot sm = (org.godot.Godot) call("get_parent");
            if (sm != null) sm.call("change_state", MOVE);
        }
    }
}
