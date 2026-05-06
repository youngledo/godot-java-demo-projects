package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;

@GodotClass(name = "FSIdle", parent = "Node")
public class FSIdle extends FSOnGround {

    @Override
    public void enter() {
        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) owner.getNode("AnimationPlayer");
            if (animPlayer != null) animPlayer.play(IDLE);
        }
    }

    @Override
    public void update(double delta) {
        Vector2 inputDir = getInputDirection();
        if (inputDir.x != 0 || inputDir.y != 0) {
            org.godot.Godot sm = getParent();
            if (sm != null) sm.call("change_state", MOVE);
        }
    }
}
