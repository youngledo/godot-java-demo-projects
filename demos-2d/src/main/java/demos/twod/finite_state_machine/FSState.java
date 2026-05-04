package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.node.Node;

@GodotClass(name = "FSState", parent = "Node")
public class FSState extends Node {

    public static final String PREVIOUS = "previous";
    public static final String JUMP = "jump";
    public static final String IDLE = "idle";
    public static final String MOVE = "move";
    public static final String STAGGER = "stagger";
    public static final String ATTACK = "attack";
    public static final String DIE = "die";
    public static final String DEAD = "dead";
    public static final String WALK = "walk";

    public void enter() {}
    public void exit() {}
    public boolean handleInput(Object inputEvent) { return false; }
    public void update(double delta) {}
    public void onAnimationFinished(String animName) {}
}
