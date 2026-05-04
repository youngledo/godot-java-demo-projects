package demos.misc.pause;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.OptionButton;

@GodotClass(name = "ProcessMode", parent = "OptionButton")
public class ProcessMode extends OptionButton {

    private org.godot.Godot cubeAnimation;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        cubeAnimation = (org.godot.Godot) call("get_node", "../../AnimationPlayer");
    }

    @GodotMethod
    public void _on_option_button_item_selected(long index) {
        if (cubeAnimation != null) {
            cubeAnimation.setProperty("process_mode", index);
        }
    }
}
