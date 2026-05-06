package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;
import org.godot.node.SceneTree;

/**
 * Main menu with start/options/exit buttons.
 * Port of menu/main/main_menu.gd.
 */
@GodotClass(name = "VXMainMenu", parent = "Control")
public class VXMainMenu extends Control {

    private org.godot.node.Node title;
    private org.godot.node.Node start;
    private org.godot.node.Node options;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        title = getNode("TitleScreen");
        start = getNode("StartGame");
        options = getNode("Options");
    }

    @GodotMethod
    public void OnStartPressed() {
        if (start != null) start.call("set_visible", true);
        if (title != null) title.call("set_visible", false);
    }

    @GodotMethod
    public void OnOptionsPressed() {
        if (options != null) {
            options.setProperty("prev_menu", title);
            options.call("set_visible", true);
        }
        if (title != null) title.call("set_visible", false);
    }

    @GodotMethod
    public void OnExitPressed() {
        org.godot.node.SceneTree tree = getTree();
        if (tree != null) tree.quit();
    }

    @GodotMethod
    public void OnRandomBlocksPressed() {
        setWorldType(0);
        changeScene("res://world/world.tscn");
    }

    @GodotMethod
    public void OnFlatGrassPressed() {
        setWorldType(1);
        changeScene("res://world/world.tscn");
    }

    @GodotMethod
    public void OnBackToTitlePressed() {
        if (title != null) title.call("set_visible", true);
        if (start != null) start.call("set_visible", false);
    }

    private void setWorldType(int type) {
        org.godot.node.Node settingsNode = getNode("/root/Settings");
        if (settingsNode != null) settingsNode.setProperty("world_type", type);
    }

    private void changeScene(String path) {
        org.godot.node.SceneTree tree = getTree();
        if (tree != null) tree.changeSceneToFile(path);
    }
}
