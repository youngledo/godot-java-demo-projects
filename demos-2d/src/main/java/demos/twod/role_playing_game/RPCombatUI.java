package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Signal;
import org.godot.node.Control;
import org.godot.node.PackedScene;
import org.godot.node.Node;

@GodotClass(name = "RPCombatUI", parent = "Control")
public class RPCombatUI extends Control {

    private org.godot.node.Node combatantsNode;
    private org.godot.node.PackedScene infoScene;
    private org.godot.node.Node combatantsList;
    private boolean initialized = false;

    @Signal
    public void flee() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        combatantsNode = (org.godot.node.Node) getProperty("combatants_node");
        Object infoObj = getProperty("info_scene");
        if (infoObj instanceof org.godot.node.PackedScene) infoScene = (org.godot.node.PackedScene) infoObj;

        // Connect button signals
        org.godot.node.Control attackBtn = (org.godot.node.Control) getNode("Buttons/GridContainer/Attack");
        if (attackBtn != null) {
            attackBtn.connect("button_up", new org.godot.core.Callable(this, "_on_Attack_button_up"), 0);
        }
        org.godot.node.Node defendBtn = getNode("Buttons/GridContainer/Defend");
        if (defendBtn != null) {
            defendBtn.connect("button_up", new org.godot.core.Callable(this, "_on_Defend_button_up"), 0);
        }
        org.godot.node.Node fleeBtn = getNode("Buttons/GridContainer/Flee");
        if (fleeBtn != null) {
            fleeBtn.connect("button_up", new org.godot.core.Callable(this, "_on_Flee_button_up"), 0);
        }
    }

    public void initialize() {
        if (combatantsNode == null) return;

        Object children = combatantsNode.call("get_children");
        if (children instanceof org.godot.Godot[]) {
            for (org.godot.Godot combatant : (org.godot.Godot[]) children) {
                org.godot.Godot health = (org.godot.node.Node) ((org.godot.node.Node) combatant).getNode("Health");

                org.godot.node.Node info = null;
                if (infoScene != null) {
                    info = infoScene.instantiate();
                }
                if (info == null || health == null) continue;

                org.godot.Godot healthInfo = (org.godot.node.Node) info.getNode("VBoxContainer/HealthContainer/Health");
                if (healthInfo != null) {
                    Object lifeObj = health.getProperty("life");
                    Object maxObj = health.getProperty("max_life");
                    healthInfo.setProperty("value", lifeObj instanceof Number ? ((Number) lifeObj).doubleValue() : 0);
                    healthInfo.setProperty("max_value", maxObj instanceof Number ? ((Number) maxObj).doubleValue() : 10);
                }

                org.godot.Godot nameLabel = (org.godot.node.Node) info.getNode("VBoxContainer/NameContainer/Name");
                if (nameLabel != null) {
                    Object nameObj = combatant.getProperty("name");
                    nameLabel.setProperty("text", nameObj != null ? nameObj.toString() : "");
                }

                health.connect("health_changed", new org.godot.core.Callable(healthInfo, "set_value"), 0);

                org.godot.node.Node combatantsUI = getNode("Combatants");
                if (combatantsUI != null) combatantsUI.addChild((org.godot.node.Node) info);
            }
        }

        org.godot.node.Control attackBtn = (org.godot.node.Control) getNode("Buttons/GridContainer/Attack");
        if (attackBtn != null) attackBtn.grabFocus();
    }

    public void OnAttackButtonUp() {
        if (combatantsNode == null) return;
        org.godot.Godot player = (org.godot.node.Node) combatantsNode.call("get_node", "Player");
        if (player == null) return;
        Object activeObj = player.getProperty("active");
        if (!(activeObj instanceof Boolean && (Boolean) activeObj)) return;

        org.godot.Godot opponent = (org.godot.node.Node) combatantsNode.call("get_node", "Opponent");
        if (opponent != null) player.call("attack", opponent);
    }

    public void OnDefendButtonUp() {
        if (combatantsNode == null) return;
        org.godot.Godot player = (org.godot.node.Node) combatantsNode.call("get_node", "Player");
        if (player == null) return;
        Object activeObj = player.getProperty("active");
        if (!(activeObj instanceof Boolean && (Boolean) activeObj)) return;

        player.call("defend");
    }

    public void OnFleeButtonUp() {
        if (combatantsNode == null) return;
        org.godot.Godot player = (org.godot.node.Node) combatantsNode.call("get_node", "Player");
        if (player == null) return;
        Object activeObj = player.getProperty("active");
        if (!(activeObj instanceof Boolean && (Boolean) activeObj)) return;

        player.call("flee");
        org.godot.Godot loser = (org.godot.node.Node) combatantsNode.call("get_node", "Player");
        org.godot.Godot winner = (org.godot.node.Node) combatantsNode.call("get_node", "Opponent");
        emitSignal("flee", winner, loser);
    }
}
