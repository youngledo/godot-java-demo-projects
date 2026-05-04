package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Popup;

@GodotClass(name = "MWHideOnMouseExit", parent = "Popup")
public class HideOnMouseExit extends Popup {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        // Connect mouse_exited signal
        connect("mouse_exited",
            new org.godot.core.Callable(this, "_on_mouse_exited"), 0);
    }

    @GodotMethod
    public void _on_mouse_exited() {
        call("hide");
    }
}
