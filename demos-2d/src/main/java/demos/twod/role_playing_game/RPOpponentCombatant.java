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

        org.godot.Godot timer = (org.godot.Godot) call("get_node", "Timer");
        if (timer == null) return;

        aiStarted = true;
        timer.call("start");

        // Connect timeout signal to perform attack
        timer.call("connect", "timeout", new org.godot.core.Callable(this, "on_ai_timeout"));
    }

    @GodotMethod
    public void on_ai_timeout() {
        if (!active) return;

        org.godot.Godot parent = (org.godot.Godot) call("get_parent");
        if (parent == null) return;

        // Find the first other combatant
        Object children = parent.call("get_children");
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
