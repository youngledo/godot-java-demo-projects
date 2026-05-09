package demos.loading.scene_changer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;
import org.godot.node.SceneTree;

@GodotClass(name = "SceneChangerA", parent = "Panel")
public class SceneChangerA extends Panel {

    @GodotMethod
    public void _onGotoScenePressed() {
        SceneTree tree = getTree();
        if (tree != null) {
            tree.changeSceneToFile("res://scene_b.tscn");
        }
    }
}
