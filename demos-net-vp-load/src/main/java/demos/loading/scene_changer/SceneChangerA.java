package demos.loading.scene_changer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;
import org.godot.Godot;

/**
 * Scene A - changes scene to Scene B using SceneTree.change_scene_to_file().
 */
@GodotClass(name = "SceneChangerA", parent = "Panel")
public class SceneChangerA extends Panel {

    @GodotMethod
    public void _onGotoScenePressed() {
        Godot tree = (Godot) call("get_tree");
        if (tree != null) {
            tree.call("change_scene_to_file", "res://scene_b.tscn");
        }
    }
}
