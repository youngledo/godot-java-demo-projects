package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;

@GodotClass(name = "FSAttack", parent = "Node")
public class FSAttack extends FSState {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Connect to Sword's attack_finished signal
        org.godot.Godot owner = (org.godot.Godot) getProperty("owner");
        if (owner != null) {
            org.godot.Godot sword = (org.godot.Godot) owner.call("get_node", "BodyPivot/WeaponPivot/Offset/Sword");
            if (sword != null) {
                sword.call("connect", "attack_finished", new org.godot.core.Callable(this, "on_Sword_attack_finished"));
            }
        }
    }

    @Override
    public void enter() {
        org.godot.Godot owner = (org.godot.Godot) getProperty("owner");
        if (owner != null) {
            org.godot.Godot animPlayer = (org.godot.Godot) owner.call("get_node", "AnimationPlayer");
            if (animPlayer != null) animPlayer.call("play", IDLE);
        }
    }

    @GodotMethod
    public void on_Sword_attack_finished() {
        org.godot.Godot sm = (org.godot.Godot) call("get_parent");
        if (sm != null) sm.call("change_state", PREVIOUS);
    }
}
