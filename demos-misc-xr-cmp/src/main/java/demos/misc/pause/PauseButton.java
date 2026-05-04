package demos.misc.pause;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Button;

@GodotClass(name = "PauseButton", parent = "Button")
public class PauseButton extends Button {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Ensure this Node won't be paused, allowing it to process
        // even when the SceneTree is paused.
        setProperty("process_mode", 3); // PROCESS_MODE_ALWAYS = 3
    }

    @GodotMethod
    public void _toggled(boolean isButtonPressed) {
        org.godot.Godot tree = (org.godot.Godot) call("get_tree");
        if (tree != null) {
            tree.setProperty("paused", isButtonPressed);
        }
        if (isButtonPressed) {
            setProperty("text", "Unpause");
        } else {
            setProperty("text", "Pause");
        }
    }
}
