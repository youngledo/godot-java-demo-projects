package demos.networking.multiplayer_bomber;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.Node;

@GodotClass(name = "MPBomberPlayerControls", parent = "Node")
public class MPBomberPlayerControls extends Node {

    @Export
    public Vector2 motion = new Vector2(0.0, 0.0);

    @Export
    public boolean bombing = false;

    @GodotMethod
    public void update() {
        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();

        double mx = 0.0;
        double my = 0.0;
        if ((boolean) input.call("is_action_pressed", "move_left")) mx -= 1;
        if ((boolean) input.call("is_action_pressed", "move_right")) mx += 1;
        if ((boolean) input.call("is_action_pressed", "move_up")) my -= 1;
        if ((boolean) input.call("is_action_pressed", "move_down")) my += 1;

        // Clamp values
        mx = Math.max(-1.0, Math.min(1.0, mx));
        my = Math.max(-1.0, Math.min(1.0, my));
        motion = new Vector2(mx, my);
        bombing = (boolean) input.call("is_action_pressed", "set_bomb");
    }
}
