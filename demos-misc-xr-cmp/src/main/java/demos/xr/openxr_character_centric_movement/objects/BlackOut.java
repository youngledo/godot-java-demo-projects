package demos.xr.openxr_character_centric_movement.objects;

import org.godot.annotation.GodotClass;
import org.godot.math.Color;
import org.godot.node.MeshInstance3D;
import org.godot.node.Node3D;
import org.godot.node.ShaderMaterial;

@GodotClass(name = "BlackOut", parent = "Node3D")
public class BlackOut extends Node3D {

    private double fade = 0.0;
    private ShaderMaterial material;
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
        MeshInstance3D meshInstance = getNodeAs("MeshInstance3D", MeshInstance3D.class);
        if (meshInstance == null) return;

        if (fade == 0.0) {
            meshInstance.setVisible(false);
        } else {
            if (material != null) {
                material.setShaderParameter("albedo", new Color(0.0, 0.0, 0.0, fade));
            }
            meshInstance.setVisible(true);
        }
    }

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        MeshInstance3D meshInstance = getNodeAs("MeshInstance3D", MeshInstance3D.class);
        if (meshInstance != null && meshInstance.getMaterialOverride() instanceof ShaderMaterial shaderMaterial) {
            material = shaderMaterial;
        }
        updateFade();
    }
}
