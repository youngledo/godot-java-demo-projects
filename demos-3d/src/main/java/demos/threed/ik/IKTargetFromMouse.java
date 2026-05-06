package demos.threed.ik;

import org.godot.annotation.GodotClass;
import org.godot.node.Camera3D;
import org.godot.math.Vector3;
import org.godot.node.Node;
import org.godot.node.Viewport;

@GodotClass(name = "IKTargetFromMouse", parent = "Camera3D")
public class IKTargetFromMouse extends Camera3D {

    private double movementSpeed = 12.0;
    private boolean flipAxis = false;
    private org.godot.node.Node targets;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object speedObj = getProperty("MOVEMENT_SPEED");
        if (speedObj instanceof Number) movementSpeed = ((Number) speedObj).doubleValue();

        Object flipObj = getProperty("flip_axis");
        if (flipObj instanceof Boolean) flipAxis = (Boolean) flipObj;

        targets = getNode("Targets");
    }

    @Override
    public void _process(double delta) {
        if (targets == null) return;

        org.godot.node.Viewport viewport = getViewport();
        if (viewport == null) return;

        Object mousePosObj = viewport.getMousePosition();
        if (mousePosObj == null) return;

        Object rayNormalObj = call("project_local_ray_normal", mousePosObj);
        if (!(rayNormalObj instanceof Vector3)) return;

        Vector3 mouseToWorld = ((Vector3) rayNormalObj).mul(movementSpeed);

        if (flipAxis) {
            mouseToWorld = mouseToWorld.mul(-1);
        } else {
            mouseToWorld = new Vector3(mouseToWorld.x, mouseToWorld.y, -mouseToWorld.z);
        }

        org.godot.Godot targetTransform = (org.godot.Godot) targets.call("get_transform");
        if (targetTransform != null) {
            targets.call("set_transform", targetTransform);
        }
        targets.setProperty("position", mouseToWorld);
    }
}
