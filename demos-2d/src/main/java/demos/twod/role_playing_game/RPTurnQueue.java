package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.Node;
import java.util.ArrayList;

@GodotClass(name = "RPTurnQueue", parent = "Node")
public class RPTurnQueue extends Node {

    private ArrayList<RPCombatant> queue = new ArrayList<>();
    private RPCombatant activeCombatant = null;
    private boolean waitingForTurn = false;
    private boolean initialized = false;

    @Signal
    public void activeCombatantChanged() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    public void initialize() {
        org.godot.node.Node combatantsList = (org.godot.node.Node) getProperty("combatants_list");
        if (combatantsList == null) return;

        Object children = combatantsList.getChildren();
        queue.clear();
        if (children instanceof org.godot.Godot[]) {
            for (org.godot.Godot node : (org.godot.Godot[]) children) {
                RPCombatant combatant = (RPCombatant) node;
                queue.add(combatant);
                combatant.setActive(false);
            }
        }

        if (!queue.isEmpty()) {
            activeCombatant = queue.get(0);
            connectTurnSignal();
            activeCombatant.setActive(true);
            emitSignal("active_combatant_changed", activeCombatant);
        }
    }

    private void connectTurnSignal() {
        if (activeCombatant == null) return;
        activeCombatant.connect("turn_finished", new org.godot.core.Callable(this, "on_turn_finished"), 0);
    }

    private void disconnectTurnSignal() {
        if (activeCombatant == null) return;
        activeCombatant.call("disconnect", "turn_finished", new org.godot.core.Callable(this, "on_turn_finished"));
    }

    @GodotMethod
    public void onTurnFinished() {
        if (queue.isEmpty()) return;

        disconnectTurnSignal();

        RPCombatant current = queue.remove(0);
        current.setActive(false);
        queue.add(current);

        if (!queue.isEmpty()) {
            activeCombatant = queue.get(0);
            connectTurnSignal();
            activeCombatant.setActive(true);
            emitSignal("active_combatant_changed", activeCombatant);
        }
    }

    public void removeCombatant(org.godot.node.Node combatant) {
        if (activeCombatant == combatant) {
            disconnectTurnSignal();
        }
        queue.remove(combatant);
        combatant.queueFree();
    }
}
