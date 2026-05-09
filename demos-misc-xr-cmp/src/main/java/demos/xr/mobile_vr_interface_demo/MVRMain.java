package demos.xr.mobile_vr_interface_demo;

import org.godot.annotation.GodotClass;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.SceneTree;
import org.godot.node.Viewport;
import org.godot.node.XRInterface;
import org.godot.singleton.Input;
import org.godot.singleton.XRServer;

@GodotClass(name = "MVRMain", parent = "Node3D")
public class MVRMain extends Node3D {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        XRInterface xrInterface = XRServer.singleton().findInterface("Native mobile");
        if (xrInterface != null && xrInterface.initialize()) {
            Viewport vp = getViewport();
            if (vp != null) {
                vp.setUseXr(true);
                vp.setVrsMode(2);
            }
            return;
        }

        SceneTree tree = getTree();
        if (tree != null) tree.quit();
    }

    @Override
    public void _process(double delta) {
        Input input = Input.singleton();

        double dirX = 0.0;
        double dirY = 0.0;

        if (input.isActionPressed("ui_left")) {
            dirX = -1.0;
        } else if (input.isActionPressed("ui_right")) {
            dirX = 1.0;
        }
        if (input.isActionPressed("ui_up")) {
            dirY = -1.0;
        } else if (input.isActionPressed("ui_down")) {
            dirY = 1.0;
        }

        Node3D xrOrigin = getNodeAs("XROrigin3D", Node3D.class);
        if (xrOrigin == null) return;

        Transform3D globalTransform = xrOrigin.getGlobalTransform();
        Basis basis = globalTransform.getBasis();
        Vector3 pos = globalTransform.getOrigin();
        pos = pos.add(new Vector3(basis.xx * dirX * delta, basis.xy * dirX * delta, basis.xz * dirX * delta));
        pos = pos.add(new Vector3(basis.zx * dirY * delta, basis.zy * dirY * delta, basis.zz * dirY * delta));

        xrOrigin.setGlobalTransform(new Transform3D(basis, pos));
    }
}
