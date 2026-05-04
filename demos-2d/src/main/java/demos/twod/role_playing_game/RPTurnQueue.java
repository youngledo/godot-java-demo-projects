package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.Node;
import java.util.ArrayList;

@GodotClass(name = "RPTurnQueue", parent = "Node")
public class RPTurnQueue extends Node {

    private ArrayList<org.godot.Godot> queue = new ArrayList<>();
    private org.godot.Godot activeCombatant = null;
    private boolean waitingForTurn = false;
    private boolean initialized = false;

    @Signal
    public void active_combatant_changed() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    public void initialize() {
        org.godot.Godot combatantsList = (org.godot.Godot) getProperty("combatants_list");
        if (combatantsList == null) return;

        Object children = combatantsList.call("get_children");
        queue.clear();
        if (children instanceof org.godot.Godot[]) {
            for (org.godot.Godot node : (org.godot.Godot[]) children) {
                queue.add(node);
                node.call("set_active", false);
            }
        }

        if (!queue.isEmpty()) {
            activeCombatant = queue.get(0);
            connectTurnSignal();
            activeCombatant.call("set_active", true);
            call("emit_signal", "active_combatant_changed", activeCombatant);
        }
    }

    private void connectTurnSignal() {
        if (activeCombatant == null) return;
        activeCombatant.call("connect", "turn_finished", new org.godot.core.Callable(this, "on_turn_finished"));
    }

    private void disconnectTurnSignal() {
        if (activeCombatant == null) return;
        activeCombatant.call("disconnect", "turn_finished", new org.godot.core.Callable(this, "on_turn_finished"));
    }

    @GodotMethod
    public void on_turn_finished() {
        if (queue.isEmpty()) return;

        disconnectTurnSignal();

        org.godot.Godot current = queue.remove(0);
        current.call("set_active", false);
        queue.add(current);

        if (!queue.isEmpty()) {
            activeCombatant = queue.get(0);
            connectTurnSignal();
            activeCombatant.call("set_active", true);
            call("emit_signal", "active_combatant_changed", activeCombatant);
        }
    }

    public void removeCombatant(org.godot.Godot combatant) {
        if (activeCombatant == combatant) {
            disconnectTurnSignal();
        }
        queue.remove(combatant);
        combatant.call("queue_free");
    }
}
