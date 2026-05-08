package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;

@GodotClass(name = "FSStagger", parent = "Node")
public class FSStagger extends FSState {

    @Override
    public void enter() {
        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) owner.getNode("AnimationPlayer");
            if (animPlayer != null) animPlayer.play(STAGGER);
        }
    }

    @Override
    public void onAnimationFinished(String animName) {
        FSStateMachine sm = (FSStateMachine) getParent();
        if (sm != null) sm.changeState(PREVIOUS);
    }
}
