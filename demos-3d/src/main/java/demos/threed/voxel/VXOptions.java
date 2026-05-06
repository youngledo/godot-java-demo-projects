package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.node.Control;

@GodotClass(name = "VXOptions", parent = "Control")
public class VXOptions extends Control {

    private org.godot.node.CanvasItem prevMenu;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        setProperty("visible", false);
    }

    public void OnBackPressed() {
        setProperty("visible", false);
        if (prevMenu != null) prevMenu.show();
    }

    public void setPrevMenu(org.godot.node.CanvasItem menu) {
        prevMenu = menu;
    }
}
