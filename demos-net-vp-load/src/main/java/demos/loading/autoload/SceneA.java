package demos.loading.autoload;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;

/**
 * Scene A - navigates to Scene B via the Global autoload singleton.
 */
@GodotClass(name = "SceneA", parent = "Panel")
public class SceneA extends Panel {

    @GodotMethod
    public void _onGotoScenePressed() {
        // Access the autoload singleton "global" and call goto_scene.
        org.godot.Godot global = (org.godot.Godot) call("get_node", "/root/global");
        if (global != null) {
            global.call("goto_scene", "res://scene_b.tscn");
        }
    }
}
