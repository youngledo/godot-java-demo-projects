package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Window;

@GodotClass(name = "MWWindow", parent = "Window")
public class MWWindow extends Window {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        connect("close_requested",
            new org.godot.core.Callable(this, "_on_close_requested"), 0);
    }

    @GodotMethod
    public void OnCloseRequested() {
        String className = (String) call("get_class");
        String name = (String) getProperty("name");
        System.out.println(className + " " + name + " was hidden.");
        hide();
    }
}
