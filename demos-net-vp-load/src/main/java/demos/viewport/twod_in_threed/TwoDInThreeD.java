package demos.viewport.twod_in_threed;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.SubViewport;
import org.godot.node.Node;

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
        SubViewport viewport = (SubViewport) getNode("SubViewport");
        if (viewport != null) {
            viewport.setProperty("render_target_clear_mode", 1); // CLEAR_MODE_ONCE
        }

        // Retrieve the texture and set it to the viewport quad
        org.godot.node.Node viewportQuad = getNode("ViewportQuad");
        if (viewportQuad != null) {
            Object materialOverride = viewportQuad.getProperty("material_override");
            if (materialOverride != null && viewport != null) {
                ((org.godot.Godot) materialOverride).setProperty("albedo_texture", viewport.getTexture());
            }
        }

        // Store camera base rotation
        org.godot.node.Camera3D camera = (org.godot.node.Camera3D) getNode("Camera3D");
        if (camera != null) {
            cameraBaseRotation = (Vector3) camera.getProperty("rotation");
        }
    }

    @Override
    public void _process(double delta) {
        counter += delta;

        org.godot.node.Camera3D camera = (org.godot.node.Camera3D) getNode("Camera3D");
        if (camera != null && cameraBaseRotation != null) {
            double rx = cameraBaseRotation.getY() + Math.cos(counter) * CAMERA_IDLE_SCALE;
            double ry = cameraBaseRotation.getY() + Math.sin(counter) * CAMERA_IDLE_SCALE;
            double rz = cameraBaseRotation.getY() + Math.sin(counter) * CAMERA_IDLE_SCALE;
            camera.setProperty("rotation", new Vector3(rx, ry, rz));
        }
    }
}
