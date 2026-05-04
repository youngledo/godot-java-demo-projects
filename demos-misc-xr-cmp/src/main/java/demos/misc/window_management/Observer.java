package demos.misc.window_management;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;

@GodotClass(name = "WindowObserver", parent = "CharacterBody3D")
public class Observer extends CharacterBody3D {

    private static final double MOUSE_SENSITIVITY = 3.0;

    public long state = 0; // MENU = 0, GRAB = 1
    private Vector2 rPos = new Vector2();
    private org.godot.Godot camera;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        camera = (org.godot.Godot) call("get_node", "Camera3D");
    }

    @Override
    public void _process(double delta) {
        if (state != 1) return; // GRAB

        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
        double xMovement = (double) input.call("get_axis", "move_left", "move_right");
        double zMovement = (double) input.call("get_axis", "move_forward", "move_backwards");

        Vector3 dir = direction(new Vector3(xMovement, 0, zMovement));
        Transform3D transform = (Transform3D) getProperty("transform");
        if (transform != null && dir != null) {
            Vector3 origin = transform.getOrigin();
            Vector3 newOrigin = new Vector3(
                origin.getX() + dir.getX() * 10 * delta,
                origin.getY() + dir.getY() * 10 * delta,
                origin.getZ() + dir.getZ() * 10 * delta
            );
            Transform3D newTransform = new Transform3D(transform.getBasis(), newOrigin);
            setProperty("transform", newTransform);
        }

        double d = delta * 0.1;
        // Yaw
        call("rotate", Vector3.UP, d * rPos.getX());
        // Pitch
        if (camera != null) {
            Transform3D camTransform = (Transform3D) camera.getProperty("transform");
            if (camTransform != null) {
                Transform3D rotated = camTransform.multiply(Transform3D.rotated(Vector3.RIGHT, d * rPos.getY()));
                camera.setProperty("transform", rotated);
            }
        }

        rPos = new Vector2();
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof org.godot.Godot) {
            org.godot.Godot evt = (org.godot.Godot) inputEvent;
            String className = (String) evt.call("get_class");

            if ("InputEventMouseMotion".equals(className)) {
                Vector2 screenRelative = (Vector2) evt.getProperty("screen_relative");
                if (screenRelative != null) {
                    rPos = new Vector2(
                        -screenRelative.getX() * MOUSE_SENSITIVITY,
                        -screenRelative.getY() * MOUSE_SENSITIVITY
                    );
                }
            }

            if ("InputEventKey".equals(className)) {
                org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
                boolean pressed = (boolean) evt.getProperty("pressed");
                boolean echo = (boolean) evt.getProperty("echo");
                if ((boolean) input.call("is_action_pressed", "ui_cancel") && pressed && !echo) {
                    if (state == 1) { // GRAB
                        input.setProperty("mouse_mode", 0); // MOUSE_MODE_VISIBLE
                        state = 0; // MENU
                    } else {
                        input.setProperty("mouse_mode", 2); // MOUSE_MODE_CAPTURED
                        state = 1; // GRAB
                    }
                }
            }
        }
        return false;
    }

    private Vector3 direction(Vector3 vector) {
        if (camera == null) return vector;
        Transform3D globalTransform = (Transform3D) camera.call("get_global_transform");
        if (globalTransform == null) return vector;
        org.godot.math.Basis basis = globalTransform.getBasis();
        // Multiply basis by vector
        Vector3 result = basis.apply(vector);
        if (result != null) {
            double len = result.length();
            if (len > 0) {
                result = new Vector3(result.getX() / len, result.getY() / len, result.getZ() / len);
            }
        }
        return result;
    }

    @GodotMethod
    public void _on_transparent_check_button_toggled(boolean buttonPressed) {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        if (!(boolean) ds.call("has_feature", 5)) { // FEATURE_WINDOW_TRANSPARENCY
            org.godot.singleton.OS.singleton().call("alert",
                "Window transparency is not supported by the current display server (" +
                ds.call("get_name") + ").");
            return;
        }

        org.godot.Godot viewport = (org.godot.Godot) call("get_viewport");
        if (viewport != null) {
            viewport.setProperty("transparent_bg", buttonPressed);
        }
        ds.call("window_set_flag", 3, buttonPressed); // WINDOW_FLAG_TRANSPARENT = 3
    }
}
