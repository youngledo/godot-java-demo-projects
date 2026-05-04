package demos.threed.ik;

import org.godot.annotation.GodotClass;
import org.godot.node.Button;

@GodotClass(name = "IKSceneButton", parent = "Button")
public class IKSceneButton extends Button {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        String scenePath = (String) getProperty("scene_to_change_to");
        if (scenePath != null && !scenePath.isEmpty()) {
            call("connect", "pressed", this, "change_scene");
        }
    }

    public void change_scene() {
        String scenePath = (String) getProperty("scene_to_change_to");
        if (scenePath != null && !scenePath.isEmpty()) {
            org.godot.Godot sceneTree = (org.godot.Godot) call("get_tree");
            if (sceneTree != null) {
                sceneTree.call("change_scene_to_file", scenePath);
            }
        }
    }
}
