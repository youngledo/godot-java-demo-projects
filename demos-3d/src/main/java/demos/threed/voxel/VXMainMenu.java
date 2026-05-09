package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.SceneTree;

@GodotClass(name = "VXMainMenu", parent = "Control")
public class VXMainMenu extends Control {

    private Control title;
    private Control start;
    private Control options;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        title = getNodeAs("TitleScreen", Control.class);
        start = getNodeAs("StartGame", Control.class);
        options = getNodeAs("Options", Control.class);
    }

    @GodotMethod
    public void OnStartPressed() {
        if (start != null) start.setVisible(true);
        if (title != null) title.setVisible(false);
    }

    @GodotMethod
    public void OnOptionsPressed() {
        if (options != null) {
            options.setProperty("prev_menu", title);
            options.setVisible(true);
        }
        if (title != null) title.setVisible(false);
    }

    @GodotMethod
    public void OnExitPressed() {
        SceneTree tree = getTree();
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
        if (title != null) title.setVisible(true);
        if (start != null) start.setVisible(false);
    }

    private void setWorldType(int type) {
        VXSettings settings = getNodeAs("/root/Settings", VXSettings.class);
        if (settings != null) settings.worldType = type;
    }

    private void changeScene(String path) {
        SceneTree tree = getTree();
        if (tree != null) tree.changeSceneToFile(path);
    }
}
