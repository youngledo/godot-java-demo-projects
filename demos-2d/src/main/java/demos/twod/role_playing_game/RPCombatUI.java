package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Signal;
import org.godot.node.Control;
import org.godot.node.PackedScene;

@GodotClass(name = "RPCombatUI", parent = "Control")
public class RPCombatUI extends Control {

    private org.godot.Godot combatantsNode;
    private org.godot.Godot infoScene;
    private org.godot.Godot combatantsList;
    private boolean initialized = false;

    @Signal
    public void flee() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        combatantsNode = (org.godot.Godot) getProperty("combatants_node");
        Object infoObj = getProperty("info_scene");
        if (infoObj instanceof org.godot.Godot) infoScene = (org.godot.Godot) infoObj;

        // Connect button signals
        org.godot.Godot attackBtn = (org.godot.Godot) call("get_node", "Buttons/GridContainer/Attack");
        if (attackBtn != null) {
            attackBtn.call("connect", "button_up", new org.godot.core.Callable(this, "_on_Attack_button_up"));
        }
        org.godot.Godot defendBtn = (org.godot.Godot) call("get_node", "Buttons/GridContainer/Defend");
        if (defendBtn != null) {
            defendBtn.call("connect", "button_up", new org.godot.core.Callable(this, "_on_Defend_button_up"));
        }
        org.godot.Godot fleeBtn = (org.godot.Godot) call("get_node", "Buttons/GridContainer/Flee");
        if (fleeBtn != null) {
            fleeBtn.call("connect", "button_up", new org.godot.core.Callable(this, "_on_Flee_button_up"));
        }
    }

    public void initialize() {
        if (combatantsNode == null) return;

        Object children = combatantsNode.call("get_children");
        if (children instanceof org.godot.Godot[]) {
            for (org.godot.Godot combatant : (org.godot.Godot[]) children) {
                org.godot.Godot health = (org.godot.Godot) combatant.call("get_node", "Health");

                org.godot.Godot info = null;
                if (infoScene != null) {
                    info = (org.godot.Godot) infoScene.call("instantiate");
                }
                if (info == null || health == null) continue;

                org.godot.Godot healthInfo = (org.godot.Godot) info.call("get_node", "VBoxContainer/HealthContainer/Health");
                if (healthInfo != null) {
                    Object lifeObj = health.getProperty("life");
                    Object maxObj = health.getProperty("max_life");
                    healthInfo.setProperty("value", lifeObj instanceof Number ? ((Number) lifeObj).doubleValue() : 0);
                    healthInfo.setProperty("max_value", maxObj instanceof Number ? ((Number) maxObj).doubleValue() : 10);
                }

                org.godot.Godot nameLabel = (org.godot.Godot) info.call("get_node", "VBoxContainer/NameContainer/Name");
                if (nameLabel != null) {
                    Object nameObj = combatant.getProperty("name");
                    nameLabel.setProperty("text", nameObj != null ? nameObj.toString() : "");
                }

                health.call("connect", "health_changed", new org.godot.core.Callable(healthInfo, "set_value"));

                org.godot.Godot combatantsUI = (org.godot.Godot) call("get_node", "Combatants");
                if (combatantsUI != null) combatantsUI.call("add_child", info);
            }
        }

        org.godot.Godot attackBtn = (org.godot.Godot) call("get_node", "Buttons/GridContainer/Attack");
        if (attackBtn != null) attackBtn.call("grab_focus");
    }

    public void _on_Attack_button_up() {
        if (combatantsNode == null) return;
        org.godot.Godot player = (org.godot.Godot) combatantsNode.call("get_node", "Player");
        if (player == null) return;
        Object activeObj = player.getProperty("active");
        if (!(activeObj instanceof Boolean && (Boolean) activeObj)) return;

        org.godot.Godot opponent = (org.godot.Godot) combatantsNode.call("get_node", "Opponent");
        if (opponent != null) player.call("attack", opponent);
    }

    public void _on_Defend_button_up() {
        if (combatantsNode == null) return;
        org.godot.Godot player = (org.godot.Godot) combatantsNode.call("get_node", "Player");
        if (player == null) return;
        Object activeObj = player.getProperty("active");
        if (!(activeObj instanceof Boolean && (Boolean) activeObj)) return;

        player.call("defend");
    }

    public void _on_Flee_button_up() {
        if (combatantsNode == null) return;
        org.godot.Godot player = (org.godot.Godot) combatantsNode.call("get_node", "Player");
        if (player == null) return;
        Object activeObj = player.getProperty("active");
        if (!(activeObj instanceof Boolean && (Boolean) activeObj)) return;

        player.call("flee");
        org.godot.Godot loser = (org.godot.Godot) combatantsNode.call("get_node", "Player");
        org.godot.Godot winner = (org.godot.Godot) combatantsNode.call("get_node", "Opponent");
        call("emit_signal", "flee", winner, loser);
    }
}
