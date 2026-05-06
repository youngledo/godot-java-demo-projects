package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;

@GodotClass(name = "RPOpponentCombatant", parent = "Node")
public class RPOpponentCombatant extends RPCombatant {

    private boolean aiStarted = false;

    @Override
    public void setActive(boolean value) {
        super.setActive(value);
        if (!active) return;

        org.godot.node.Timer timer = (org.godot.node.Timer) getNode("Timer");
        if (timer == null) return;

        aiStarted = true;
        timer.start();

        // Connect timeout signal to perform attack
        timer.connect("timeout", new org.godot.core.Callable(this, "on_ai_timeout"), 0);
    }

    @GodotMethod
    public void onAiTimeout() {
        if (!active) return;

        org.godot.node.Node parent = getParent();
        if (parent == null) return;

        // Find the first other combatant
        Object children = parent.getChildren();
        if (children instanceof org.godot.Godot[]) {
            for (org.godot.Godot actor : (org.godot.Godot[]) children) {
                if (actor != this) {
                    attack(actor);
                    break;
                }
            }
        }
    }
}
