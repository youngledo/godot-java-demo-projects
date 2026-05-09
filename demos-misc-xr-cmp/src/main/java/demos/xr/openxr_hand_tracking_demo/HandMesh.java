package demos.xr.openxr_hand_tracking_demo;

import org.godot.annotation.GodotClass;
import org.godot.node.XRNode3D;
import org.godot.node.XRPose;
import org.godot.node.XRPositionalTracker;
import org.godot.node.XRTracker;
import org.godot.singleton.XRServer;

@GodotClass(name = "HandMesh", parent = "XRNode3D")
public class HandMesh extends XRNode3D {

    private int hand = 0;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    @Override
    public void _process(double delta) {
        String newTracker = hand == 0 ? "/user/hand_tracker/left" : "/user/hand_tracker/right";
        XRTracker handTracker = XRServer.singleton().getTracker(newTracker);
        if (handTracker != null && Boolean.TRUE.equals(handTracker.getProperty("has_tracking_data"))) {
            if (!newTracker.equals(getTracker())) {
                System.out.println("Switching to " + (hand == 0 ? "left" : "right") + " hand tracker");
                setTracker(newTracker);
                setPose("default");
            }
            return;
        }

        newTracker = hand == 0 ? "left_hand" : "right_hand";
        XRTracker controllerTracker = XRServer.singleton().getTracker(newTracker);
        if (controllerTracker instanceof XRPositionalTracker positionalTracker) {
            if (!newTracker.equals(getTracker())) {
                System.out.println("Switching to " + (hand == 0 ? "left" : "right") + " controller tracker");
                setTracker(newTracker);
            }

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
}
