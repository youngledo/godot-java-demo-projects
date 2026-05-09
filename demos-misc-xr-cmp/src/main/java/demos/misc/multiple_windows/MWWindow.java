package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.Window;

@GodotClass(name = "MWWindow", parent = "Window")
public class MWWindow extends Window {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        connect("close_requested", new Callable(this, "OnCloseRequested"), 0);
    }

    @GodotMethod
    public void OnCloseRequested() {
        System.out.println(getGodotClassName() + " " + getName() + " was hidden.");
        hide();
    }
}
