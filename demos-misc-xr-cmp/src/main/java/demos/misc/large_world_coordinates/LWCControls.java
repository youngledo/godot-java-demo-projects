package demos.misc.large_world_coordinates;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.annotation.GodotMethod;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.VBoxContainer;

@GodotClass(name = "LWCControls", parent = "VBoxContainer")
public class LWCControls extends VBoxContainer {

    private static final double ROT_SPEED = 0.003;
    private static final double ZOOM_SPEED = 0.5;
    private static final int MAIN_BUTTONS = 1 | 4 | 2; // LEFT | MIDDLE | RIGHT

    @Export
    public org.godot.Godot camera;
    @Export
    public org.godot.Godot cameraHolder;
    @Export
    public org.godot.Godot rotationX;
    @Export
    public org.godot.Godot nodeToMove;
    @Export
    public org.godot.Godot rigidBody;

    private double zoom = 7.0;
    private double rotX = 0.0;
    private double rotY = 0.0;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        if (camera != null) {
            Object pos = camera.getProperty("position");
            if (pos instanceof Vector3) {
                zoom = ((Vector3) pos).getZ();
            }
        }

        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        org.godot.Godot helpLabel = (org.godot.Godot) call("get_node", "%HelpLabel");
        if ((boolean) os.call("has_feature", "double")) {
            if (helpLabel != null) {
                helpLabel.setProperty("text",
                    "Double precision is enabled in this engine build.\n" +
                    "No shaking should occur at high coordinate levels\n" +
                    "(±65,536 or more on any axis).");
                helpLabel.call("add_theme_color_override", "font_color",
                    new org.godot.math.Color(0.667f, 1.0f, 0.667f));
            }
        }
    }

    @Override
    public void _process(double delta) {
        org.godot.Godot coordinates = (org.godot.Godot) call("get_node", "%Coordinates");
        org.godot.Godot incrementX = (org.godot.Godot) call("get_node", "%IncrementX");
        org.godot.Godot incrementY = (org.godot.Godot) call("get_node", "%IncrementY");
        org.godot.Godot incrementZ = (org.godot.Godot) call("get_node", "%IncrementZ");

        if (nodeToMove != null && coordinates != null) {
            Vector3 pos = (Vector3) nodeToMove.getProperty("position");
            if (pos != null) {
                String text = String.format(
                    "X: [color=#fb9]%f[/color]\nY: [color=#bfa]%f[/color]\nZ: [color=#9cf]%f[/color]",
                    pos.getX(), pos.getY(), pos.getZ());
                coordinates.setProperty("text", text);
            }

            if (incrementX != null && (boolean) incrementX.getProperty("button_pressed")) {
                Vector3 curPos = (Vector3) nodeToMove.getProperty("position");
                nodeToMove.setProperty("position", new Vector3(curPos.getX() + 10000.0 * delta, curPos.getY(), curPos.getZ()));
            }
            if (incrementY != null && (boolean) incrementY.getProperty("button_pressed")) {
                Vector3 curPos = (Vector3) nodeToMove.getProperty("position");
                nodeToMove.setProperty("position", new Vector3(curPos.getX(), curPos.getY() + 100000.0 * delta, curPos.getZ()));
            }
            if (incrementZ != null && (boolean) incrementZ.getProperty("button_pressed")) {
                Vector3 curPos = (Vector3) nodeToMove.getProperty("position");
                nodeToMove.setProperty("position", new Vector3(curPos.getX(), curPos.getY(), curPos.getZ() + 1000000.0 * delta));
            }
        }
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof org.godot.Godot) {
            org.godot.Godot evt = (org.godot.Godot) inputEvent;
            String className = (String) evt.call("get_class");

            if ("InputEventMouseButton".equals(className)) {
                long buttonIndex = (long) evt.getProperty("button_index");
                if (buttonIndex == 4) { // WHEEL_UP
                    zoom -= ZOOM_SPEED;
                }
                if (buttonIndex == 5) { // WHEEL_DOWN
                    zoom += ZOOM_SPEED;
                }
                zoom = Math.max(4, Math.min(15, zoom));
                if (camera != null) {
                    Object camPos = camera.getProperty("position");
                    if (camPos instanceof Vector3) {
                        Vector3 p = (Vector3) camPos;
                        camera.setProperty("position", new Vector3(p.getX(), p.getY(), zoom));
                    }
                }
            }

            if ("InputEventMouseMotion".equals(className)) {
                long buttonMask = (long) evt.getProperty("button_mask");
                if ((buttonMask & MAIN_BUTTONS) != 0) {
                    Vector2 relative = (Vector2) evt.getProperty("screen_relative");
                    if (relative != null) {
                        rotY -= relative.getX() * ROT_SPEED;
                        rotX -= relative.getY() * ROT_SPEED;
                        rotX = Math.max(-1.4, Math.min(0.16, rotX));
                        if (cameraHolder != null) {
                            cameraHolder.setProperty("transform",
                                new Transform3D(Basis.fromEuler(new Vector3(0, rotY, 0)), new Vector3()));
                        }
                        if (rotationX != null) {
                            rotationX.setProperty("transform",
                                new Transform3D(Basis.fromEuler(new Vector3(rotX, 0, 0)), new Vector3()));
                        }
                    }
                }
            }
        }
        return false;
    }

    @GodotMethod
    public void _on_go_to_button_pressed(long xPosition) {
        if (nodeToMove == null) return;
        if (xPosition == 0) {
            nodeToMove.setProperty("position", Vector3.ZERO);
        } else {
            Vector3 curPos = (Vector3) nodeToMove.getProperty("position");
            nodeToMove.setProperty("position", new Vector3((double) xPosition, curPos.getY(), curPos.getZ()));
        }
    }

    @GodotMethod
    public void _on_open_documentation_pressed() {
        org.godot.singleton.OS.singleton().call("shell_open",
            "https://docs.godotengine.org/en/latest/tutorials/physics/large_world_coordinates.html");
    }
}
