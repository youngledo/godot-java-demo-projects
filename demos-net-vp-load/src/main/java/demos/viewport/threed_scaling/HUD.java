package demos.viewport.threed_scaling;

import org.godot.annotation.GodotClass;
import org.godot.node.Control;
import org.godot.node.Label;
import org.godot.singleton.Input;

@GodotClass(name = "HUD", parent = "Control")
public class HUD extends Control {

    private int scaleFactor = 1;
    private int filterMode = 0; // Viewport.SCALING_3D_MODE_BILINEAR

    private org.godot.Godot viewport;
    private Label scaleLabel;
    private Label filterLabel;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        viewport = (org.godot.Godot) call("get_tree");
        if (viewport != null) {
            viewport = (org.godot.Godot) viewport.getProperty("root");
        }

        scaleLabel = (Label) call("get_node", "VBoxContainer/Scale");
        filterLabel = (Label) call("get_node", "VBoxContainer/Filter");

        if (viewport != null) {
            viewport.setProperty("scaling_3d_mode", filterMode);
        }
    }

    @Override
    public boolean _unhandledInput(Object inputEvent) {
        if (inputEvent instanceof org.godot.Godot) {
            org.godot.Godot evt = (org.godot.Godot) inputEvent;

            if ((boolean) evt.call("is_action_pressed", "cycle_viewport_resolution", false)) {
                scaleFactor = wrapInt(scaleFactor + 1, 1, 5);
                if (viewport != null) {
                    viewport.setProperty("scaling_3d_scale", 1.0 / scaleFactor);
                }
                if (scaleLabel != null) {
                    scaleLabel.setProperty("text", String.format("Scale: %3.0f%%", 100.0 / scaleFactor));
                }
            }

            if ((boolean) evt.call("is_action_pressed", "toggle_filtering", false)) {
                filterMode = wrapInt(filterMode + 1, 0, 5);
                if (viewport != null) {
                    viewport.setProperty("scaling_3d_mode", filterMode);
                }
                if (filterLabel != null) {
                    String[] names = {"Bilinear", "Fsr", "Fsr2", "Bilinear Max", "Orthogonal"};
                    String filterName = (filterMode < names.length) ? names[filterMode] : "Unknown";
                    filterLabel.setProperty("text", "Scaling 3D Mode: " + filterName);
                }
            }
        }
        return false;
    }

    private static int wrapInt(int value, int min, int max) {
        if (value >= max) return min;
        return value;
    }
}
