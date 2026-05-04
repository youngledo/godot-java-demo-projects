package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.node.Control;

@GodotClass(name = "VXOptionButtons", parent = "Control")
public class VXOptionButtons extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Connect slider signal
        org.godot.Godot slider = (org.godot.Godot) call("get_node", "GridContainer/RenderDistance/Slider");
        if (slider != null) {
            slider.call("connect", "value_changed", this, "on_render_distance_changed");
        }

        org.godot.Godot checkbox = (org.godot.Godot) call("get_node", "GridContainer/Fog/CheckBox");
        if (checkbox != null) {
            checkbox.call("connect", "toggled", this, "on_fog_toggled");
        }
    }

    public void on_render_distance_changed(double value) {
        org.godot.Godot settings = (org.godot.Godot) call("get_node", "/root/Settings");
        if (settings != null) {
            settings.setProperty("render_distance", (int) value);
            settings.call("save_settings");
        }
    }

    public void on_fog_toggled(boolean enabled) {
        org.godot.Godot settings = (org.godot.Godot) call("get_node", "/root/Settings");
        if (settings != null) {
            settings.setProperty("fog_enabled", enabled);
            settings.call("save_settings");
        }
    }
}
