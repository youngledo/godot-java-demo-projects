package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;

@GodotClass(name = "FSDie", parent = "Node")
public class FSDie extends FSState {

    @Override
    public void enter() {
        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            owner.call("set_dead", true);
            org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) owner.getNode("AnimationPlayer");
            if (animPlayer != null) animPlayer.play(DIE);
        }
    }

    @Override
    public void onAnimationFinished(String animName) {
        org.godot.Godot sm = getParent();
        if (sm != null) sm.call("change_state", DEAD);
    }
}
