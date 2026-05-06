package demos.misc.pause;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.OptionButton;
import org.godot.node.Node;

@GodotClass(name = "ProcessMode", parent = "OptionButton")
public class ProcessMode extends OptionButton {

    private org.godot.node.AnimationPlayer cubeAnimation;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        cubeAnimation = (org.godot.node.AnimationPlayer) getNode("../../AnimationPlayer");
    }

    @GodotMethod
    public void OnOptionButtonItemSelected(long index) {
        if (cubeAnimation != null) {
            cubeAnimation.setProperty("process_mode", index);
        }
    }
}
