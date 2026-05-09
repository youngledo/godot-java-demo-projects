package demos.loading.scene_changer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.PackedScene;
import org.godot.node.Panel;
import org.godot.node.Resource;
import org.godot.node.SceneTree;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "SceneChangerB", parent = "Panel")
public class SceneChangerB extends Panel {

    @GodotMethod
    public void _onGotoScenePressed() {
        Resource resource = ResourceLoader.singleton().load("res://scene_a.tscn");
        if (resource instanceof PackedScene scene) {
            SceneTree tree = getTree();
            if (tree != null) {
                tree.changeSceneToPacked(scene);
            }
        }
    }
}
