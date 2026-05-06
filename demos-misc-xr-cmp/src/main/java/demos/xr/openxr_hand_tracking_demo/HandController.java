package demos.xr.openxr_hand_tracking_demo;

import org.godot.annotation.GodotClass;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "HandController", parent = "XRController3D")
public class HandController extends org.godot.Godot {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    @Override
    public void _process(double delta) {
        String trackerName = (String) getProperty("tracker");
        Object trackerObj = call("get_tracker", trackerName);
        if (trackerObj == null) return;
        org.godot.Godot tracker = (org.godot.Godot) trackerObj;

        String newPose = "palm_pose";
        Object xrPoseObj = tracker.call("get_pose", newPose);
        if (xrPoseObj != null) {
            org.godot.Godot xrPose = (org.godot.Godot) xrPoseObj;
            int confidence = (int) xrPose.getProperty("tracking_confidence");
            if (confidence == 0) { // XR_TRACKING_CONFIDENCE_NONE
                newPose = "grip";
            }
        } else {
            newPose = "grip";
        }

        String currentPose = (String) getProperty("pose");
        if (!newPose.equals(currentPose)) {
            setProperty("pose", newPose);
        }
    }
}
