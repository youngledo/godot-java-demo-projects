package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.node.Label;

@GodotClass(name = "VXSplashText", parent = "Label")
public class VXSplashText extends Label {

    private double time = 0;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    @Override
    public void _process(double delta) {
        time += delta;
        double scale = 1.0 + 0.1 * Math.sin(time * 2);
        setScale(new org.godot.math.Vector2(scale, scale));
    }
}
