package demos.xr.openxr_origin_centric_movement.objects;

import org.godot.annotation.GodotClass;
import org.godot.node.Node3D;

@GodotClass(name = "OXOCBlackOut", parent = "Node3D")
public class OXOCBlackOut extends Node3D {

    private double fade = 0.0;
    private org.godot.Godot material;
    private boolean initialized = false;

    public double getFade() {
        return fade;
    }

    public void setFade(double value) {
        fade = value;
        if (initialized) {
            updateFade();
        }
    }

    private void updateFade() {
        org.godot.Godot meshInstance = (org.godot.Godot) call("get_node", "MeshInstance3D");
        if (meshInstance == null) return;

        if (fade == 0.0) {
            meshInstance.setProperty("visible", false);
        } else {
            if (material != null) {
                org.godot.math.Color albedo = new org.godot.math.Color(0.0, 0.0, 0.0, fade);
                material.call("set_shader_parameter", "albedo", albedo);
            }
            meshInstance.setProperty("visible", true);
        }
    }

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.Godot meshInstance = (org.godot.Godot) call("get_node", "MeshInstance3D");
        if (meshInstance != null) {
            material = (org.godot.Godot) meshInstance.getProperty("material_override");
        }
        updateFade();
    }
}
