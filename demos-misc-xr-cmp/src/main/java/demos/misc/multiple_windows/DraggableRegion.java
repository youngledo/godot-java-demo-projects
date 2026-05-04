package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.node.Area2D;

@GodotClass(name = "MWDraggableRegion", parent = "Area2D")
public class DraggableRegion extends Area2D {

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof org.godot.Godot) {
            org.godot.Godot evt = (org.godot.Godot) inputEvent;
            String className = (String) evt.call("get_class");
            if ("InputEventMouseButton".equals(className)) {
                boolean pressed = (boolean) evt.getProperty("pressed");
                if (pressed) {
                    org.godot.Godot win = (org.godot.Godot) call("get_window");
                    if (win != null) win.call("start_drag");
                }
            }
        }
        return false;
    }
}
