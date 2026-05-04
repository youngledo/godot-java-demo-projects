package demos.viewport.twod_in_threed;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.SubViewport;

@GodotClass(name = "TwoDInThreeD", parent = "Node3D")
public class TwoDInThreeD extends Node3D {

    private static final double CAMERA_IDLE_SCALE = 0.005;

    private double counter = 0.0;
    private Vector3 cameraBaseRotation;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Clear the viewport
        SubViewport viewport = (SubViewport) call("get_node", "SubViewport");
        if (viewport != null) {
            viewport.setProperty("render_target_clear_mode", 1); // CLEAR_MODE_ONCE
        }

        // Retrieve the texture and set it to the viewport quad
        org.godot.Godot viewportQuad = (org.godot.Godot) call("get_node", "ViewportQuad");
        if (viewportQuad != null) {
            Object materialOverride = viewportQuad.getProperty("material_override");
            if (materialOverride != null && viewport != null) {
                ((org.godot.Godot) materialOverride).setProperty("albedo_texture", viewport.call("get_texture"));
            }
        }

        // Store camera base rotation
        org.godot.Godot camera = (org.godot.Godot) call("get_node", "Camera3D");
        if (camera != null) {
            cameraBaseRotation = (Vector3) camera.getProperty("rotation");
        }
    }

    @Override
    public void _process(double delta) {
        counter += delta;

        org.godot.Godot camera = (org.godot.Godot) call("get_node", "Camera3D");
        if (camera != null && cameraBaseRotation != null) {
            double rx = cameraBaseRotation.getY() + Math.cos(counter) * CAMERA_IDLE_SCALE;
            double ry = cameraBaseRotation.getY() + Math.sin(counter) * CAMERA_IDLE_SCALE;
            double rz = cameraBaseRotation.getY() + Math.sin(counter) * CAMERA_IDLE_SCALE;
            camera.setProperty("rotation", new Vector3(rx, ry, rz));
        }
    }
}
