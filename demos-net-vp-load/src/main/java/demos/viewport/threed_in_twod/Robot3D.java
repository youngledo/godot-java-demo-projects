package demos.viewport.threed_in_twod;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector3;
import org.godot.node.Node3D;

@GodotClass(name = "Robot3D", parent = "Node3D")
public class Robot3D extends Node3D {

    private org.godot.Godot model;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        model = (org.godot.Godot) call("get_node", "Model");
    }

    @Override
    public void _process(double delta) {
        if (model == null) return;

        // Rotate the model
        Vector3 rotation = (org.godot.math.Vector3) model.getProperty("rotation");
        if (rotation != null) {
            model.setProperty("rotation", new org.godot.math.Vector3(
                rotation.getX(),
                rotation.getY() + delta * 0.7,
                rotation.getZ()
            ));
        }
    }
}
