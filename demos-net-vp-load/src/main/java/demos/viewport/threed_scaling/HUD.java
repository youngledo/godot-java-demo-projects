package demos.viewport.threed_scaling;

import org.godot.annotation.GodotClass;
import org.godot.node.Control;
import org.godot.node.InputEvent;
import org.godot.node.Label;
import org.godot.node.Viewport;

@GodotClass(name = "HUD", parent = "Control")
public class HUD extends Control {

    private int scaleFactor = 1;
    private int filterMode = 0;

    private Viewport viewport;
    private Label scaleLabel;
    private Label filterLabel;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        viewport = getViewport();
        scaleLabel = getNodeAs("VBoxContainer/Scale", Label.class);
        filterLabel = getNodeAs("VBoxContainer/Filter", Label.class);

        if (viewport != null) {
            viewport.setScaling3dMode(filterMode);
        }
    }

    @Override
    public boolean _unhandledInput(Object inputEvent) {
        if (inputEvent instanceof InputEvent evt) {
            if (evt.isActionPressed("cycle_viewport_resolution", false)) {
                scaleFactor = wrapInt(scaleFactor + 1, 1, 5);
                if (viewport != null) {
                    viewport.setScaling3dScale(1.0 / scaleFactor);
                }
                if (scaleLabel != null) {
                    scaleLabel.setText(String.format("Scale: %3.0f%%", 100.0 / scaleFactor));
                }
            }

            if (evt.isActionPressed("toggle_filtering", false)) {
                filterMode = wrapInt(filterMode + 1, 0, 5);
                if (viewport != null) {
                    viewport.setScaling3dMode(filterMode);
                }
                if (filterLabel != null) {
                    String[] names = {"Bilinear", "Fsr", "Fsr2", "Bilinear Max", "Orthogonal"};
                    String filterName = (filterMode < names.length) ? names[filterMode] : "Unknown";
                    filterLabel.setText("Scaling 3D Mode: " + filterName);
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
