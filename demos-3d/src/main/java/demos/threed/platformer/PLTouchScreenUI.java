package demos.threed.platformer;

import org.godot.annotation.GodotClass;
import org.godot.node.CanvasLayer;
import org.godot.singleton.DisplayServer;

@GodotClass(name = "PLTouchScreenUI", parent = "CanvasLayer")
public class PLTouchScreenUI extends CanvasLayer {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        hide();
        if (DisplayServer.singleton().isTouchscreenAvailable()) {
            show();
        }
    }
}
