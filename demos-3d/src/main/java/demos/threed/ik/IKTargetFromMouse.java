package demos.threed.ik;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Camera3D;
import org.godot.node.Node;
import org.godot.node.Node3D;
import org.godot.node.Viewport;

@GodotClass(name = "IKTargetFromMouse", parent = "Camera3D")
public class IKTargetFromMouse extends Camera3D {

    private double movementSpeed = 12.0;
    private boolean flipAxis = false;
    private Node3D targets;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object speedObj = getProperty("MOVEMENT_SPEED");
        if (speedObj instanceof Number number) movementSpeed = number.doubleValue();

        Object flipObj = getProperty("flip_axis");
        if (flipObj instanceof Boolean value) flipAxis = value;

        Node node = getNode("Targets");
        if (node instanceof Node3D node3D) {
            targets = node3D;
        }
    }

    @Override
    public void _process(double delta) {
        if (targets == null) return;

        Viewport viewport = getViewport();
        if (viewport == null) return;

        Vector2 mousePos = viewport.getMousePosition();
        Vector3 mouseToWorld = projectLocalRayNormal(mousePos).mul(movementSpeed);

        if (flipAxis) {
            mouseToWorld = mouseToWorld.mul(-1);
        } else {
            mouseToWorld = new Vector3(mouseToWorld.x, mouseToWorld.y, -mouseToWorld.z);
        }

        targets.setPosition(mouseToWorld);
    }
}
