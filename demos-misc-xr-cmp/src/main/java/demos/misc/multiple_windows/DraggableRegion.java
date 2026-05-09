package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.node.Area2D;
import org.godot.node.InputEventMouseButton;
import org.godot.node.Window;

@GodotClass(name = "MWDraggableRegion", parent = "Area2D")
public class DraggableRegion extends Area2D {

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof InputEventMouseButton event && event.isPressed()) {
            Window window = getWindow();
            if (window != null) window.startDrag();
        }
        return false;
    }
}
