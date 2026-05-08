package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.Node;

@GodotClass(name = "FSPlayerStateMachine", parent = "Node")
public class FSPlayerStateMachine extends FSStateMachine {

    private boolean initialized = false;

    @Signal
    public void stateChanged() {}

    @Override
    public void _enterTree() {
        if (initialized) return;
        initialized = true;

        Object children = getChildren();
        if (children instanceof org.godot.Godot[]) {
            for (org.godot.Godot child : (org.godot.Godot[]) children) {
                Object nameObj = child.getProperty("name");
                String name = nameObj != null ? nameObj.toString() : "";
                statesMap.put(name, (FSState) child);
            }
        }

        // Connect to AnimationPlayer's animation_finished signal
        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            org.godot.Godot animPlayer = (org.godot.node.Node) owner.getNode("AnimationPlayer");
            if (animPlayer != null) {
                animPlayer.connect("animation_finished", new org.godot.core.Callable(this, "_on_animation_finished"), 0);
            }
        }

        initialize(FSState.IDLE);
    }

    @GodotMethod
    public void OnAnimationFinished(String animName) {
        if (!active || currentState == null) return;
        currentState.onAnimationFinished(animName);
    }

    @Override
    public boolean _unhandledInput(Object inputEvent) {
        if (!active || currentState == null) return false;

        // Check for attack input globally
        org.godot.Godot input = (org.godot.Godot) inputEvent;
        if (input != null) {
            Object pressed = input.call("is_action_pressed", "attack");
            if (pressed instanceof Boolean && (Boolean) pressed) {
                Object currentName = currentState.getProperty("name");
                String name = currentName != null ? currentName.toString() : "";
                if (name.equals(FSState.ATTACK) || name.equals(FSState.STAGGER)) {
                    return false;
                }
                pushState(FSState.ATTACK);
                return true;
            }
        }

        return currentState.handleInput(inputEvent);
    }
}
