package demos.misc.window_management;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Camera3D;
import org.godot.node.CharacterBody3D;
import org.godot.node.InputEventKey;
import org.godot.node.InputEventMouseMotion;
import org.godot.node.Viewport;
import org.godot.singleton.DisplayServer;
import org.godot.singleton.Input;
import org.godot.singleton.OS;

@GodotClass(name = "WindowObserver", parent = "CharacterBody3D")
public class Observer extends CharacterBody3D {

    private static final double MOUSE_SENSITIVITY = 3.0;

    public long state = 0;
    private Vector2 rPos = new Vector2();
    private Camera3D camera;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        camera = getNodeAs("Camera3D", Camera3D.class);
    }

    @Override
    public void _process(double delta) {
        if (state != 1) return;

        Input input = Input.singleton();
        double xMovement = input.getAxis("move_left", "move_right");
        double zMovement = input.getAxis("move_forward", "move_backwards");

        Vector3 dir = direction(new Vector3(xMovement, 0, zMovement));
        Transform3D transform = getTransform();
        if (dir != null) {
            Vector3 origin = transform.getOrigin();
            Vector3 newOrigin = new Vector3(
                origin.x + dir.x * 10 * delta,
                origin.y + dir.y * 10 * delta,
                origin.z + dir.z * 10 * delta
            );
            setTransform(new Transform3D(transform.getBasis(), newOrigin));
        }

        double d = delta * 0.1;
        rotate(Vector3.UP, d * rPos.x);
        if (camera != null) {
            Transform3D camTransform = camera.getTransform();
            Transform3D rotated = camTransform.multiply(Transform3D.rotated(Vector3.RIGHT, d * rPos.y));
            camera.setTransform(rotated);
        }

        rPos = new Vector2();
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof InputEventMouseMotion event) {
            Vector2 screenRelative = event.getScreenRelative();
            rPos = new Vector2(-screenRelative.x * MOUSE_SENSITIVITY, -screenRelative.y * MOUSE_SENSITIVITY);
        }

        if (inputEvent instanceof InputEventKey event) {
            Input input = Input.singleton();
            if (input.isActionPressed("ui_cancel") && event.isPressed() && !event.isEcho()) {
                if (state == 1) {
                    input.setMouseMode(0);
                    state = 0;
                } else {
                    input.setMouseMode(2);
                    state = 1;
                }
            }
        }
        return false;
    }

    private Vector3 direction(Vector3 vector) {
        if (camera == null) return vector;
        Transform3D globalTransform = camera.getGlobalTransform();
        Basis basis = globalTransform.getBasis();
        Vector3 result = basis.apply(vector);
        if (result != null) {
            double len = result.length();
            if (len > 0) {
                result = new Vector3(result.x / len, result.y / len, result.z / len);
            }
        }
        return result;
    }

    @GodotMethod
    public void OnTransparentCheckButtonToggled(boolean buttonPressed) {
        DisplayServer displayServer = DisplayServer.singleton();
        if (!displayServer.hasFeature(5)) {
            OS.singleton().alert("Window transparency is not supported by the current display server ("
                + displayServer.getName() + ").");
            return;
        }

        Viewport viewport = getViewport();
        if (viewport != null) {
            viewport.setTransparentBg(buttonPressed);
        }
        displayServer.windowSetFlag(3, buttonPressed);
    }
}
