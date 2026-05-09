package demos.xr.openxr_hand_tracking_demo;

import org.godot.annotation.GodotClass;
import org.godot.node.XRController3D;
import org.godot.node.XRPose;
import org.godot.node.XRPositionalTracker;
import org.godot.node.XRTracker;
import org.godot.singleton.XRServer;

@GodotClass(name = "HandController", parent = "XRController3D")
public class HandController extends XRController3D {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    @Override
    public void _process(double delta) {
        XRTracker tracker = XRServer.singleton().getTracker(getTracker());
        if (!(tracker instanceof XRPositionalTracker positionalTracker)) return;

        String newPose = "palm_pose";
        XRPose xrPose = positionalTracker.getPose(newPose);
        if (xrPose == null || xrPose.getTrackingConfidence() == 0) {
            newPose = "grip";
        }

        if (!newPose.equals(getPose())) {
            setPose(newPose);
        }
    }
}
