package demos.loading.autoload;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;

/**
 * Scene B - navigates to Scene A via the Global autoload singleton.
 */
@GodotClass(name = "SceneB", parent = "Panel")
public class SceneB extends Panel {

    @GodotMethod
    public void _onGotoScenePressed() {
        // Access the autoload singleton "global" and call goto_scene.
        org.godot.Godot global = (org.godot.Godot) call("get_node", "/root/global");
        if (global != null) {
            global.call("goto_scene", "res://scene_a.tscn");
        }
    }
}
