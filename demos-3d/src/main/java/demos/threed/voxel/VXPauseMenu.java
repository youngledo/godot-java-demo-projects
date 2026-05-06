package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.node.Control;
import org.godot.node.Node;
import org.godot.node.SceneTree;
import org.godot.singleton.Input;

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

        Object justPressed = (boolean) input.isActionJustPressed("pause");
        if (justPressed instanceof Boolean && (Boolean) justPressed) {
            Object vis = getProperty("visible");
            boolean visible = vis instanceof Boolean && (Boolean) vis;
            setProperty("visible", !visible);

            if (!visible) {
                input.setMouseMode(0); // VISIBLE
            } else {
                input.setMouseMode(2); // CAPTURED
            }
        }
    }

    public void OnResumePressed() {
        setProperty("visible", false);
        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
        if (input != null) input.setMouseMode(2);
    }

    public void OnOptionsPressed() {
        org.godot.node.CanvasItem options = (org.godot.node.CanvasItem) getNode("Options");
        if (options != null) options.show();
    }

    public void OnMainMenuPressed() {
        org.godot.node.SceneTree tree = getTree();
        if (tree != null) tree.changeSceneToFile("res://menu/main/main_menu.tscn");
    }

    public void OnExitPressed() {
        org.godot.node.SceneTree tree = getTree();
        if (tree != null) tree.quit();
    }
}
