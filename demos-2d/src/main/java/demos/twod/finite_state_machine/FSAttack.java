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
        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            org.godot.Godot sword = (org.godot.Godot) owner.getNode("BodyPivot/WeaponPivot/Offset/Sword");
            if (sword != null) {
                sword.connect("attack_finished", new org.godot.core.Callable(this, "on_Sword_attack_finished"), 0);
            }
        }
    }

    @Override
    public void enter() {
        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) owner.getNode("AnimationPlayer");
            if (animPlayer != null) animPlayer.play(IDLE);
        }
    }

    @GodotMethod
    public void onSwordAttackFinished() {
        org.godot.Godot sm = getParent();
        if (sm != null) sm.call("change_state", PREVIOUS);
    }
}
