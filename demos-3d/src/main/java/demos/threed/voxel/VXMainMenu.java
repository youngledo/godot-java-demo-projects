package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

/**
 * Main menu with start/options/exit buttons.
 * Port of menu/main/main_menu.gd.
 */
@GodotClass(name = "VXMainMenu", parent = "Control")
public class VXMainMenu extends Control {

    private org.godot.Godot title;
    private org.godot.Godot start;
    private org.godot.Godot options;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        title = (org.godot.Godot) call("get_node", "TitleScreen");
        start = (org.godot.Godot) call("get_node", "StartGame");
        options = (org.godot.Godot) call("get_node", "Options");
    }

    @GodotMethod
    public void _on_Start_pressed() {
        if (start != null) start.call("set_visible", true);
        if (title != null) title.call("set_visible", false);
    }

    @GodotMethod
    public void _on_Options_pressed() {
        if (options != null) {
            options.setProperty("prev_menu", title);
            options.call("set_visible", true);
        }
        if (title != null) title.call("set_visible", false);
    }

    @GodotMethod
    public void _on_Exit_pressed() {
        org.godot.Godot tree = (org.godot.Godot) call("get_tree");
        if (tree != null) tree.call("quit");
    }

    @GodotMethod
    public void _on_RandomBlocks_pressed() {
        setWorldType(0);
        changeScene("res://world/world.tscn");
    }

    @GodotMethod
    public void _on_FlatGrass_pressed() {
        setWorldType(1);
        changeScene("res://world/world.tscn");
    }

    @GodotMethod
    public void _on_BackToTitle_pressed() {
        if (title != null) title.call("set_visible", true);
        if (start != null) start.call("set_visible", false);
    }

    private void setWorldType(int type) {
        org.godot.Godot settingsNode = (org.godot.Godot) call("get_node", "/root/Settings");
        if (settingsNode != null) settingsNode.setProperty("world_type", type);
    }

    private void changeScene(String path) {
        org.godot.Godot tree = (org.godot.Godot) call("get_tree");
        if (tree != null) tree.call("change_scene_to_file", path);
    }
}
