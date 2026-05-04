package demos.xr.mobile_vr_interface_demo;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.singleton.Input;

@GodotClass(name = "MVRMain", parent = "Node3D")
public class MVRMain extends Node3D {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Find the mobile VR interface.
        Object xrInterface = call("find_interface", "Native mobile");
        if (xrInterface != null) {
            boolean initialized2 = (boolean) ((org.godot.Godot) xrInterface).call("initialize");
            if (initialized2) {
                // Set up viewport.
                org.godot.Godot vp = (org.godot.Godot) call("get_viewport");
                if (vp != null) {
                    vp.setProperty("use_xr", true);
                    vp.setProperty("vrs_mode", 2); // Viewport.VRS_XR
                }
                return;
            }
        }

        // How did we get here?
        org.godot.Godot tree = (org.godot.Godot) call("get_tree");
        if (tree != null) tree.call("quit");
    }

    @Override
    public void _process(double delta) {
        Input input = Input.singleton();
        if (input == null) return;

        double dirX = 0.0;
        double dirY = 0.0;

        if ((boolean) input.call("is_action_pressed", "ui_left", false)) {
            dirX = -1.0;
        } else if ((boolean) input.call("is_action_pressed", "ui_right", false)) {
            dirX = 1.0;
        }
        if ((boolean) input.call("is_action_pressed", "ui_up", false)) {
            dirY = -1.0;
        } else if ((boolean) input.call("is_action_pressed", "ui_down", false)) {
            dirY = 1.0;
        }

        org.godot.Godot xrOrigin = (org.godot.Godot) call("get_node", "XROrigin3D");
        if (xrOrigin == null) return;

        Object globalTransformObj = xrOrigin.call("get_global_transform");
        if (!(globalTransformObj instanceof org.godot.math.Transform3D)) return;
        org.godot.math.Transform3D globalTransform = (org.godot.math.Transform3D) globalTransformObj;
        org.godot.math.Basis basis = globalTransform.getBasis();

        // Move along basis X and Z directions.
        Vector3 pos = globalTransform.getOrigin();
        pos = pos.add(new Vector3(basis.xx * dirX * delta, basis.xy * dirX * delta, basis.xz * dirX * delta));
        pos = pos.add(new Vector3(basis.zx * dirY * delta, basis.zy * dirY * delta, basis.zz * dirY * delta));

        // Update the global position.
        globalTransform = new org.godot.math.Transform3D(basis, pos);
        xrOrigin.call("set_global_transform", globalTransform);
    }
}
