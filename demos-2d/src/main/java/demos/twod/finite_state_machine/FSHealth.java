package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;

@GodotClass(name = "FSHealth", parent = "Node")
public class FSHealth extends Node {
    @GodotMethod
    public void takeDamage(double amount, Object effect) {
    }
}
