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

    private org.godot.node.Node animationPlayback;
    private org.godot.node.Node healthNode;
    private boolean initialized = false;

    @Signal
    public void turnFinished() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object dmgObj = getProperty("damage");
        if (dmgObj instanceof Number) damage = ((Number) dmgObj).intValue();
        Object defObj = getProperty("defense");
        if (defObj instanceof Number) defense = ((Number) defObj).intValue();

        animationPlayback = getNode("Sprite2D/AnimationTree");
        healthNode = getNode("Health");
    }

    public void setActive(boolean value) {
        active = value;
        setProcess(value);
        setProcessInput(value);
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
        if (target instanceof org.godot.node.Node) {
            org.godot.node.Node tgt = (org.godot.node.Node) target;
            RPHealth targetHealth = tgt.getNodeAs("Health", RPHealth.class);
            if (targetHealth != null) {
                targetHealth.takeDamage(damage);
            }
            org.godot.Godot targetAnimTree = (org.godot.node.Node) tgt.getNode("Sprite2D/AnimationTree");
            if (targetAnimTree != null) {
                Object playback = targetAnimTree.getProperty("parameters/playback");
                if (playback instanceof org.godot.Godot) {
                    ((org.godot.Godot) playback).call("start", "take_damage");
                }
            }
        }
        emitSignal("turn_finished");
    }

    @GodotMethod
    public void defend() {
        if (healthNode != null) {
            Object armorObj = healthNode.getProperty("armor");
            int armor = armorObj instanceof Number ? ((Number) armorObj).intValue() : 0;
            healthNode.setProperty("armor", armor + defense);
        }
        emitSignal("turn_finished");
    }

    @GodotMethod
    public void flee() {
        emitSignal("turn_finished");
    }
}
