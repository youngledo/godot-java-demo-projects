package demos.threed.ik;

import org.godot.annotation.GodotClass;
import org.godot.node.Button;
import org.godot.node.SceneTree;

@GodotClass(name = "IKSceneButton", parent = "Button")
public class IKSceneButton extends Button {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        String scenePath = (String) getProperty("scene_to_change_to");
        if (scenePath != null && !scenePath.isEmpty()) {
		connect("pressed", new org.godot.core.Callable(this, "change_scene"), 0);
        }
    }

    public void changeScene() {
        String scenePath = (String) getProperty("scene_to_change_to");
        if (scenePath != null && !scenePath.isEmpty()) {
            org.godot.node.SceneTree sceneTree = getTree();
            if (sceneTree != null) {
                sceneTree.changeSceneToFile(scenePath);
            }
        }
    }
}
