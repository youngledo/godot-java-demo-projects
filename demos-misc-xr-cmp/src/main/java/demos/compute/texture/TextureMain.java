package demos.compute.texture;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Basis;
import org.godot.node.BaseButton;
import org.godot.node.HSlider;
import org.godot.node.Node3D;

@GodotClass(name = "TextureMain", parent = "Node3D")
public class TextureMain extends Node3D {

    private double y = 0.0;
    private WaterPlane waterPlane;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        waterPlane = getNodeAs("WaterPlane", WaterPlane.class);

        HSlider rainSlider = getNodeAs("Container/RainSize/HSlider", HSlider.class);
        if (rainSlider != null && waterPlane != null) {
            rainSlider.setValue(waterPlane.getRainSize());
        }

        HSlider mouseSlider = getNodeAs("Container/MouseSize/HSlider", HSlider.class);
        if (mouseSlider != null && waterPlane != null) {
            mouseSlider.setValue(waterPlane.getMouseSize());
        }
    }

    @Override
    public void _process(double delta) {
        BaseButton rotateCheckbox = getNodeAs("Container/Rotate", BaseButton.class);
        if (rotateCheckbox != null && rotateCheckbox.isButtonPressed() && waterPlane != null) {
            y += delta;
            waterPlane.setBasis(Basis.fromAxisAngleY(y));
        }
    }

    @GodotMethod
    public void OnRainSizeChanged(double value) {
        if (waterPlane != null) {
            waterPlane.setRainSize(value);
        }
    }

    @GodotMethod
    public void OnMouseSizeChanged(double value) {
        if (waterPlane != null) {
            waterPlane.setMouseSize(value);
        }
    }
}
