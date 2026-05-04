package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;

@GodotClass(name = "FSDie", parent = "Node")
public class FSDie extends FSState {

    @Override
    public void enter() {
        org.godot.Godot owner = (org.godot.Godot) getProperty("owner");
        if (owner != null) {
            owner.call("set_dead", true);
            org.godot.Godot animPlayer = (org.godot.Godot) owner.call("get_node", "AnimationPlayer");
            if (animPlayer != null) animPlayer.call("play", DIE);
        }
    }

    @Override
    public void onAnimationFinished(String animName) {
        org.godot.Godot sm = (org.godot.Godot) call("get_parent");
        if (sm != null) sm.call("change_state", DEAD);
    }
}
