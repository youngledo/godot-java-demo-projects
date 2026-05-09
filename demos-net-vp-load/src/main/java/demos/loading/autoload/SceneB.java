package demos.loading.autoload;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;

@GodotClass(name = "SceneB", parent = "Panel")
public class SceneB extends Panel {

    @GodotMethod
    public void _onGotoScenePressed() {
        Global global = getNodeAs("/root/global", Global.class);
        if (global != null) {
            global.gotoScene("res://scene_a.tscn");
        }
    }
}
