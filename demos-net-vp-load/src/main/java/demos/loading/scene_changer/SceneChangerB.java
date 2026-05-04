package demos.loading.scene_changer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;
import org.godot.Godot;

/**
 * Scene B - changes scene to Scene A using SceneTree.change_scene_to_packed().
 * Loads the scene as a PackedScene first, then uses change_scene_to_packed().
 */
@GodotClass(name = "SceneChangerB", parent = "Panel")
public class SceneChangerB extends Panel {

    @GodotMethod
    public void _onGotoScenePressed() {
        // Load the scene as a PackedScene.
        Object scene = call("load", "res://scene_a.tscn");
        if (scene != null) {
            Godot tree = (Godot) call("get_tree");
            if (tree != null) {
                tree.call("change_scene_to_packed", scene);
            }
        }
    }
}
