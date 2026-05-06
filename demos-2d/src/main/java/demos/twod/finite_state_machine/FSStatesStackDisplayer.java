package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.node.Panel;
import org.godot.node.Node;

@GodotClass(name = "FSStatesStackDisplayer", parent = "Panel")
public class FSStatesStackDisplayer extends Panel {

    private org.godot.node.Node fsmNode;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        fsmNode = getNode("../../Player/StateMachine");
    }

    @Override
    public void _process(double delta) {
        if (fsmNode == null) return;

        // Use getProperty to access states_stack since it's a field
        Object stackObj = fsmNode.getProperty("states_stack");
        if (stackObj == null) return;

        // Build display text
        StringBuilder statesNames = new StringBuilder();
        StringBuilder numbers = new StringBuilder();

        // Try to get stack as array
        if (stackObj instanceof org.godot.Godot[]) {
            org.godot.Godot[] stack = (org.godot.Godot[]) stackObj;
            for (int i = 0; i < stack.length; i++) {
                Object nameObj = stack[i].getProperty("name");
                statesNames.append(nameObj != null ? nameObj.toString() : "").append("\n");
                numbers.append(i).append("\n");
            }
        }

        org.godot.node.Node statesLabel = getNode("States");
        org.godot.node.Node numbersLabel = getNode("Numbers");
        if (statesLabel != null) statesLabel.setProperty("text", statesNames.toString());
        if (numbersLabel != null) numbersLabel.setProperty("text", numbers.toString());
    }
}
