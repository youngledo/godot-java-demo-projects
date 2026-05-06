package demos.loading.scene_changer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;
import org.godot.Godot;

/**
 * Scene B - changes scene to Scene A using SceneTree.changeSceneToPacked().
 * Loads the scene as a PackedScene first, then uses changeSceneToPacked().
 */
@GodotClass(name = "SceneChangerB", parent = "Panel")
public class SceneChangerB extends Panel {

    @GodotMethod
    public void _onGotoScenePressed() {
        // Load the scene as a PackedScene.
        org.godot.node.PackedScene scene = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://scene_a.tscn");
        if (scene != null) {
            Godot tree = (Godot) getTree();
            if (tree != null) {
                tree.call("change_scene_to_packed", scene);
            }
        }
    }
}
