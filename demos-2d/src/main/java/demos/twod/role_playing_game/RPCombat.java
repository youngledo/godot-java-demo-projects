package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.Node;

@GodotClass(name = "RPCombat", parent = "Node")
public class RPCombat extends Node {

    private org.godot.Godot ui;
    private boolean initialized = false;

    @Signal
    public void combat_finished() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        ui = (org.godot.Godot) call("get_node", "CombatCanvas/UI");
        if (ui != null) {
            ui.call("connect", "flee", new org.godot.core.Callable(this, "on_flee"));
        }
    }

    @GodotMethod
    public void on_flee(org.godot.Godot winner, org.godot.Godot loser) {
        finishCombat(winner, loser);
    }

    public void initialize(Object[] combatScenes) {
        org.godot.Godot combatants = (org.godot.Godot) call("get_node", "Combatants");
        if (combatants == null) return;

        for (Object sceneObj : combatScenes) {
            if (!(sceneObj instanceof org.godot.Godot)) continue;
            org.godot.Godot combatant = (org.godot.Godot) ((org.godot.Godot) sceneObj).call("instantiate");
            if (combatant == null) continue;

            combatants.call("add_child", combatant);

            org.godot.Godot health = (org.godot.Godot) combatant.call("get_node", "Health");
            if (health != null) {
                health.call("connect", "dead", new org.godot.core.Callable(this, "on_combatant_death"));
                // Store reference to combatant for death callback
                health.setProperty("combatant_ref", combatant);
            }
        }

        if (ui != null) ui.call("initialize");
        org.godot.Godot turnQueue = (org.godot.Godot) call("get_node", "TurnQueue");
        if (turnQueue != null) turnQueue.call("initialize");
    }

    public void clear_combat() {
        org.godot.Godot combatants = (org.godot.Godot) call("get_node", "Combatants");
        if (combatants != null) {
            Object children = combatants.call("get_children");
            if (children instanceof org.godot.Godot[]) {
                for (org.godot.Godot n : (org.godot.Godot[]) children) {
                    n.call("queue_free");
                }
            }
        }
        if (ui != null) {
            org.godot.Godot uiCombatants = (org.godot.Godot) ui.call("get_node", "Combatants");
            if (uiCombatants != null) {
                Object children = uiCombatants.call("get_children");
                if (children instanceof org.godot.Godot[]) {
                    for (org.godot.Godot n : (org.godot.Godot[]) children) {
                        n.call("queue_free");
                    }
                }
            }
        }
    }

    public void finishCombat(org.godot.Godot winner, org.godot.Godot loser) {
        call("emit_signal", "combat_finished", winner, loser);
    }

    @GodotMethod
    public void on_combatant_death() {
        // Find which combatant died by checking the health node
        org.godot.Godot combatants = (org.godot.Godot) call("get_node", "Combatants");
        if (combatants == null) return;

        org.godot.Godot deadCombatant = null;
        Object children = combatants.call("get_children");
        if (children instanceof org.godot.Godot[]) {
            for (org.godot.Godot n : (org.godot.Godot[]) children) {
                org.godot.Godot health = (org.godot.Godot) n.call("get_node", "Health");
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
            winner = (org.godot.Godot) combatants.call("get_node", "Player");
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
