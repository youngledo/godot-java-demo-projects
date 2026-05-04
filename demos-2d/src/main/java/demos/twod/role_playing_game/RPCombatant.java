package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.Node;

@GodotClass(name = "RPCombatant", parent = "Node")
public class RPCombatant extends Node {

    protected int damage = 1;
    protected int defense = 1;
    protected boolean active = false;

    private org.godot.Godot animationPlayback;
    private org.godot.Godot healthNode;
    private boolean initialized = false;

    @Signal
    public void turn_finished() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object dmgObj = getProperty("damage");
        if (dmgObj instanceof Number) damage = ((Number) dmgObj).intValue();
        Object defObj = getProperty("defense");
        if (defObj instanceof Number) defense = ((Number) defObj).intValue();

        animationPlayback = (org.godot.Godot) call("get_node", "Sprite2D/AnimationTree");
        healthNode = (org.godot.Godot) call("get_node", "Health");
    }

    public void setActive(boolean value) {
        active = value;
        call("set_process", value);
        call("set_process_input", value);
        if (!active) return;

        if (healthNode != null) {
            Object armorObj = healthNode.getProperty("armor");
            Object baseArmorObj = healthNode.getProperty("base_armor");
            int armor = armorObj instanceof Number ? ((Number) armorObj).intValue() : 0;
            int baseArmor = baseArmorObj instanceof Number ? ((Number) baseArmorObj).intValue() : 0;
            if (armor >= baseArmor + defense) {
                healthNode.setProperty("armor", baseArmor);
            }
        }
    }

    @GodotMethod
    public void attack(Object target) {
        if (target instanceof org.godot.Godot) {
            org.godot.Godot tgt = (org.godot.Godot) target;
            org.godot.Godot targetHealth = (org.godot.Godot) tgt.call("get_node", "Health");
            if (targetHealth != null) {
                targetHealth.call("take_damage", damage);
            }
            org.godot.Godot targetAnimTree = (org.godot.Godot) tgt.call("get_node", "Sprite2D/AnimationTree");
            if (targetAnimTree != null) {
                Object playback = targetAnimTree.call("get", "parameters/playback");
                if (playback instanceof org.godot.Godot) {
                    ((org.godot.Godot) playback).call("start", "take_damage");
                }
            }
        }
        call("emit_signal", "turn_finished");
    }

    @GodotMethod
    public void defend() {
        if (healthNode != null) {
            Object armorObj = healthNode.getProperty("armor");
            int armor = armorObj instanceof Number ? ((Number) armorObj).intValue() : 0;
            healthNode.setProperty("armor", armor + defense);
        }
        call("emit_signal", "turn_finished");
    }

    @GodotMethod
    public void flee() {
        call("emit_signal", "turn_finished");
    }
}
