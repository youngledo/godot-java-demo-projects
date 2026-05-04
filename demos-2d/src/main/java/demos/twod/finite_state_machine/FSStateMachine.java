package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Signal;
import org.godot.node.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@GodotClass(name = "FSStateMachine", parent = "Node")
public class FSStateMachine extends Node {

    protected Map<String, org.godot.Godot> statesMap = new HashMap<>();
    protected ArrayList<org.godot.Godot> statesStack = new ArrayList<>();
    protected org.godot.Godot currentState = null;
    protected boolean active = false;
    private boolean initialized = false;

    @Signal
    public void state_changed() {}

    @Override
    public void _enterTree() {
        if (initialized) return;
        initialized = true;

        // Initialize states map from children
        Object children = call("get_children");
        if (children instanceof org.godot.Godot[]) {
            for (org.godot.Godot child : (org.godot.Godot[]) children) {
                Object nameObj = child.getProperty("name");
                String name = nameObj != null ? nameObj.toString() : "";
                statesMap.put(name, child);
            }
        }

        // Start with first child
        if (!statesMap.isEmpty()) {
            String startState = statesMap.keySet().iterator().next();
            initialize(startState);
        }
    }

    public void initialize(String initialStateName) {
        active = true;
        org.godot.Godot state = statesMap.get(initialStateName);
        if (state == null) return;
        statesStack.add(0, state);
        currentState = state;
        currentState.call("enter");
    }

    @Override
    public void _physicsProcess(double delta) {
        if (!active || currentState == null) return;
        currentState.call("update", delta);
    }

    @Override
    public boolean _unhandledInput(Object inputEvent) {
        if (!active || currentState == null) return false;
        return currentState.call("handle_input", inputEvent) instanceof Boolean ?
                (Boolean) currentState.call("handle_input", inputEvent) : false;
    }

    public void changeState(String stateName) {
        if (!active || currentState == null) return;

        currentState.call("exit");

        if (FSState.PREVIOUS.equals(stateName)) {
            if (statesStack.size() > 1) statesStack.remove(0);
        } else {
            org.godot.Godot newState = statesMap.get(stateName);
            if (newState != null) {
                statesStack.set(0, newState);
            }
        }

        if (!statesStack.isEmpty()) {
            currentState = statesStack.get(0);
            call("emit_signal", "state_changed", currentState);
            updateStateNameDisplayer();
            if (!FSState.PREVIOUS.equals(stateName)) {
                currentState.call("enter");
            }
        }
    }

    public void pushState(String stateName) {
        if (!active || currentState == null) return;
        currentState.call("exit");

        org.godot.Godot newState = statesMap.get(stateName);
        if (newState != null) {
            statesStack.add(0, newState);
        }

        currentState = statesStack.get(0);
        call("emit_signal", "state_changed", currentState);
        updateStateNameDisplayer();
        currentState.call("enter");
    }

    private void updateStateNameDisplayer() {
        if (currentState == null) return;
        org.godot.Godot owner = (org.godot.Godot) getProperty("owner");
        if (owner != null) {
            org.godot.Godot label = (org.godot.Godot) owner.call("get_node_or_null", "StateNameDisplayer");
            if (label != null) {
                Object nameObj = currentState.getProperty("name");
                label.setProperty("text", nameObj != null ? nameObj.toString() : "");
            }
        }
    }

    public org.godot.Godot getCurrentState() { return currentState; }
    public ArrayList<org.godot.Godot> getStatesStack() { return statesStack; }
}
