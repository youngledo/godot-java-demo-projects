package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;

@GodotClass(name = "FSStagger", parent = "Node")
public class FSStagger extends FSState {

    @Override
    public void enter() {
        org.godot.Godot owner = (org.godot.Godot) getProperty("owner");
        if (owner != null) {
            org.godot.Godot animPlayer = (org.godot.Godot) owner.call("get_node", "AnimationPlayer");
            if (animPlayer != null) animPlayer.call("play", STAGGER);
        }
    }

    @Override
    public void onAnimationFinished(String animName) {
        org.godot.Godot sm = (org.godot.Godot) call("get_parent");
        if (sm != null) sm.call("change_state", PREVIOUS);
    }
}
