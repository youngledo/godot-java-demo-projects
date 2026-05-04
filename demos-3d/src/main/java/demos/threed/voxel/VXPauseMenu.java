package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.node.Control;

@GodotClass(name = "VXPauseMenu", parent = "Control")
public class VXPauseMenu extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        setProperty("visible", false);
    }

    @Override
    public void _process(double delta) {
        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
        if (input == null) return;

        Object justPressed = input.call("is_action_just_pressed", "pause");
        if (justPressed instanceof Boolean && (Boolean) justPressed) {
            Object vis = getProperty("visible");
            boolean visible = vis instanceof Boolean && (Boolean) vis;
            setProperty("visible", !visible);

            if (!visible) {
                input.call("set_mouse_mode", 0); // VISIBLE
            } else {
                input.call("set_mouse_mode", 2); // CAPTURED
            }
        }
    }

    public void _on_Resume_pressed() {
        setProperty("visible", false);
        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
        if (input != null) input.call("set_mouse_mode", 2);
    }

    public void _on_Options_pressed() {
        org.godot.Godot options = (org.godot.Godot) call("get_node", "Options");
        if (options != null) options.call("show");
    }

    public void _on_MainMenu_pressed() {
        org.godot.Godot tree = (org.godot.Godot) call("get_tree");
        if (tree != null) tree.call("change_scene_to_file", "res://menu/main/main_menu.tscn");
    }

    public void _on_Exit_pressed() {
        org.godot.Godot tree = (org.godot.Godot) call("get_tree");
        if (tree != null) tree.call("quit");
    }
}
