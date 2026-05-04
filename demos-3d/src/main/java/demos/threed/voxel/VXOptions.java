package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.node.Control;

@GodotClass(name = "VXOptions", parent = "Control")
public class VXOptions extends Control {

    private org.godot.Godot prevMenu;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        setProperty("visible", false);
    }

    public void _on_Back_pressed() {
        setProperty("visible", false);
        if (prevMenu != null) prevMenu.call("show");
    }

    public void setPrevMenu(org.godot.Godot menu) {
        prevMenu = menu;
    }
}
