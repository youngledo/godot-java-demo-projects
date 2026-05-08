package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Signal;
import org.godot.node.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@GodotClass(name = "FSStateMachine", parent = "Node")
public class FSStateMachine extends Node {

    protected Map<String, FSState> statesMap = new HashMap<>();
    protected ArrayList<FSState> statesStack = new ArrayList<>();
    protected FSState currentState = null;
    protected boolean active = false;
    private boolean initialized = false;

    @Signal
    public void stateChanged() {}

    @Override
    public void _enterTree() {
        if (initialized) return;
        initialized = true;

        // Initialize states map from children
        Object children = getChildren();
        if (children instanceof org.godot.Godot[]) {
            for (org.godot.Godot child : (org.godot.Godot[]) children) {
                if (child instanceof FSState) {
                    Object nameObj = child.getProperty("name");
                    String name = nameObj != null ? nameObj.toString() : "";
                    statesMap.put(name, (FSState) child);
                }
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
        FSState state = statesMap.get(initialStateName);
        if (state == null) return;
        statesStack.add(0, state);
        currentState = state;
        currentState.enter();
    }

    @Override
    public void _physicsProcess(double delta) {
        if (!active || currentState == null) return;
        currentState.update(delta);
    }

    @Override
    public boolean _unhandledInput(Object inputEvent) {
        if (!active || currentState == null) return false;
        return currentState.handleInput(inputEvent);
    }

    public void changeState(String stateName) {
        if (!active || currentState == null) return;

        currentState.exit();

        if (FSState.PREVIOUS.equals(stateName)) {
            if (statesStack.size() > 1) statesStack.remove(0);
        } else {
            FSState newState = statesMap.get(stateName);
            if (newState != null) {
                statesStack.set(0, newState);
            }
        }

        if (!statesStack.isEmpty()) {
            currentState = statesStack.get(0);
            emitSignal("state_changed", currentState);
            updateStateNameDisplayer();
            if (!FSState.PREVIOUS.equals(stateName)) {
                currentState.enter();
            }
        }
    }

    public void pushState(String stateName) {
        if (!active || currentState == null) return;
        currentState.exit();

        FSState newState = statesMap.get(stateName);
        if (newState != null) {
            statesStack.add(0, newState);
        }

        currentState = statesStack.get(0);
        emitSignal("state_changed", currentState);
        updateStateNameDisplayer();
        currentState.enter();
    }

    private void updateStateNameDisplayer() {
        if (currentState == null) return;
        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            org.godot.Godot label = (org.godot.node.Node) owner.getNodeOrNull("StateNameDisplayer");
            if (label != null) {
                Object nameObj = currentState.getProperty("name");
                label.setProperty("text", nameObj != null ? nameObj.toString() : "");
            }
        }
    }

    public FSState getCurrentState() { return currentState; }
    public ArrayList<FSState> getStatesStack() { return statesStack; }
}
