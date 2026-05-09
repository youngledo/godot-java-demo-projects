package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.CanvasLayer;
import org.godot.node.Node;

@GodotClass(name = "RPCombat", parent = "Node")
public class RPCombat extends Node {

    private RPCombatUI ui;
    private boolean initialized = false;

    @Signal
    public void combatFinished() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        ui = (RPCombatUI) getNode("CombatCanvas/UI");
        if (ui != null) {
            ui.connect("flee", new org.godot.core.Callable(this, "on_flee"), 0);
        }
    }

    @GodotMethod
    public void onFlee(org.godot.Godot winner, org.godot.Godot loser) {
        finishCombat(winner, loser);
    }

    public void show() {
        CanvasLayer canvas = getNodeAs("CombatCanvas", CanvasLayer.class);
        if (canvas != null) canvas.show();
    }

    public void initialize(Object[] combatScenes) {
        org.godot.node.Node combatants = getNode("Combatants");
        if (combatants == null) return;

        for (Object sceneObj : combatScenes) {
            if (!(sceneObj instanceof org.godot.Godot)) continue;
            org.godot.node.Node combatant = (org.godot.node.Node) ((org.godot.node.PackedScene) sceneObj).instantiate();
            if (combatant == null) continue;

            combatants.addChild((org.godot.node.Node) combatant);

            org.godot.Godot health = (org.godot.node.Node) combatant.getNode("Health");
            if (health != null) {
                health.connect("dead", new org.godot.core.Callable(this, "on_combatant_death"), 0);
                // Store reference to combatant for death callback
                health.setProperty("combatant_ref", combatant);
            }
        }

        if (ui != null) ui.initialize();
        RPTurnQueue turnQueue = (RPTurnQueue) getNode("TurnQueue");
        if (turnQueue != null) turnQueue.initialize();
    }

    public void clearCombat() {
        org.godot.node.Node combatants = getNode("Combatants");
        if (combatants != null) {
            Object children = combatants.getChildren();
            if (children instanceof org.godot.Godot[]) {
                for (org.godot.Godot n : (org.godot.Godot[]) children) {
                    ((org.godot.node.Node) n).queueFree();
                }
            }
        }
        if (ui != null) {
            org.godot.node.Node uiCombatants = (org.godot.node.Node) ui.getNode("Combatants");
            if (uiCombatants != null) {
                Object children = uiCombatants.getChildren();
                if (children instanceof org.godot.Godot[]) {
                    for (org.godot.Godot n : (org.godot.Godot[]) children) {
                        ((org.godot.node.Node) n).queueFree();
                    }
                }
            }
        }
    }

    public void finishCombat(org.godot.Godot winner, org.godot.Godot loser) {
        emitSignal("combat_finished", winner, loser);
    }

    @GodotMethod
    public void onCombatantDeath() {
        // Find which combatant died by checking the health node
        org.godot.node.Node combatants = getNode("Combatants");
        if (combatants == null) return;

        org.godot.Godot deadCombatant = null;
        Object children = combatants.getChildren();
        if (children instanceof org.godot.Godot[]) {
            for (org.godot.Godot n : (org.godot.Godot[]) children) {
                org.godot.node.Node health = (org.godot.node.Node) ((org.godot.node.Node) n).getNode("Health");
                if (health != null) {
                    Object lifeObj = health.getProperty("life");
                    if (lifeObj instanceof Number && ((Number) lifeObj).intValue() <= 0) {
                        deadCombatant = n;
                        break;
                    }
                }
            }
        }
        if (deadCombatant == null) return;

        org.godot.Godot winner = null;
        Object nameObj = deadCombatant.getProperty("name");
        String deadName = nameObj != null ? nameObj.toString() : "";

        if (!deadName.equals("Player")) {
            winner = (org.godot.Godot) combatants.getNode("Player");
        } else {
            if (children instanceof org.godot.Godot[]) {
                for (org.godot.Godot n : (org.godot.Godot[]) children) {
                    Object nNameObj = n.getProperty("name");
                    if (!"Player".equals(nNameObj != null ? nNameObj.toString() : "")) {
                        winner = n;
                        break;
                    }
                }
            }
        }

        finishCombat(winner, deadCombatant);
    }
}
