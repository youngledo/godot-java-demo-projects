package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.core.Callable;
import org.godot.node.CheckBox;
import org.godot.node.Control;
import org.godot.node.Slider;

@GodotClass(name = "VXOptionButtons", parent = "Control")
public class VXOptionButtons extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Slider slider = getNodeAs("GridContainer/RenderDistance/Slider", Slider.class);
        if (slider != null) {
            slider.connect("value_changed", new Callable(this, "on_render_distance_changed"), 0);
        }

        CheckBox checkbox = getNodeAs("GridContainer/Fog/CheckBox", CheckBox.class);
        if (checkbox != null) {
            checkbox.connect("toggled", new Callable(this, "on_fog_toggled"), 0);
        }
    }

    public void onRenderDistanceChanged(double value) {
        VXSettings settings = getNodeAs("/root/Settings", VXSettings.class);
        if (settings != null) {
            settings.renderDistance = (int) value;
            settings.saveSettings();
        }
    }

    public void onFogToggled(boolean enabled) {
        VXSettings settings = getNodeAs("/root/Settings", VXSettings.class);
        if (settings != null) {
            settings.fogEnabled = enabled;
            settings.saveSettings();
        }
    }
}
