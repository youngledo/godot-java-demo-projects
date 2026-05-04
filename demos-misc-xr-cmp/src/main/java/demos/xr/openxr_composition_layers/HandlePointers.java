package demos.xr.openxr_composition_layers;

import org.godot.annotation.GodotClass;
import org.godot.node.Node3D;

@GodotClass(name = "HandlePointers", parent = "Node3D")
public class HandlePointers extends Node3D {

    @Override
    public void _ready() {
    }

    @Override
    public boolean _input(Object event) {
        return false;
    }

    @Override
    public void _process(double delta) {
    }
}
