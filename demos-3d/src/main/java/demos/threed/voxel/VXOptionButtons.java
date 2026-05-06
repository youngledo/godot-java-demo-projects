package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "VXOptionButtons", parent = "Control")
public class VXOptionButtons extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Connect slider signal
        org.godot.node.Slider slider = (org.godot.node.Slider) getNode("GridContainer/RenderDistance/Slider");
        if (slider != null) {
            slider.connect("value_changed", new org.godot.core.Callable(this, "on_render_distance_changed"), 0);
        }

        org.godot.node.CheckBox checkbox = (org.godot.node.CheckBox) getNode("GridContainer/Fog/CheckBox");
        if (checkbox != null) {
            checkbox.connect("toggled", new org.godot.core.Callable(this, "on_fog_toggled"), 0);
        }
    }

    public void onRenderDistanceChanged(double value) {
        org.godot.node.Node settings = getNode("/root/Settings");
        if (settings != null) {
            settings.setProperty("render_distance", (int) value);
            settings.call("save_settings");
        }
    }

    public void onFogToggled(boolean enabled) {
        org.godot.node.Node settings = getNode("/root/Settings");
        if (settings != null) {
            settings.setProperty("fog_enabled", enabled);
            settings.call("save_settings");
        }
    }
}
