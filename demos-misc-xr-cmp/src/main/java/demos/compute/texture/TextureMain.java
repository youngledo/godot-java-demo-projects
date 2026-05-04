package demos.compute.texture;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node3D;
import org.godot.math.Vector3;

/**
 * Port of compute/texture/main.gd
 *
 * Controls for the water ripple compute shader demo.
 * Manages rotation, rain size, and mouse size parameters.
 */
@GodotClass(name = "TextureMain", parent = "Node3D")
public class TextureMain extends Node3D {

    private double y = 0.0;
    private Object waterPlane;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        waterPlane = call("get_node", "WaterPlane");

        // Set slider values to match water plane defaults
        Object rainSlider = call("get_node", "Container/RainSize/HSlider");
        if (rainSlider != null && waterPlane != null) {
            Object rainSize = callOn(waterPlane, "get", "rain_size");
            callOn(rainSlider, "set", "value", rainSize);
        }

        Object mouseSlider = call("get_node", "Container/MouseSize/HSlider");
        if (mouseSlider != null && waterPlane != null) {
            Object mouseSize = callOn(waterPlane, "get", "mouse_size");
            callOn(mouseSlider, "set", "value", mouseSize);
        }
    }

    @Override
    public void _process(double delta) {
        Object rotateCheckbox = call("get_node", "Container/Rotate");
        if (rotateCheckbox != null) {
            boolean pressed = (boolean) callOn(rotateCheckbox, "get", "button_pressed");
            if (pressed && waterPlane != null) {
                y += delta;
                // Create a Basis from rotation around Y axis
                double sin = Math.sin(y);
                double cos = Math.cos(y);
                // Basis(Vector3.UP, angle) creates a rotation matrix
                // We use call to construct it
                callOn(waterPlane, "set", "basis",
                    call("Basis.new", new Vector3(0, 1, 0), y));
            }
        }
    }

    @GodotMethod
    public void _on_rain_size_changed(double value) {
        if (waterPlane != null) {
            callOn(waterPlane, "set", "rain_size", value);
        }
    }

    @GodotMethod
    public void _on_mouse_size_changed(double value) {
        if (waterPlane != null) {
            callOn(waterPlane, "set", "mouse_size", value);
        }
    }

    private Object callOn(Object obj, String method, Object... args) {
        if (obj instanceof org.godot.Godot) {
            return ((org.godot.Godot) obj).call(method, args);
        }
        return null;
    }
}
