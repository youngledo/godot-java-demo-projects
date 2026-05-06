package demos.xr.openxr_hand_tracking_demo;

import org.godot.annotation.GodotClass;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "HandMesh", parent = "XRNode3D")
public class HandMesh extends org.godot.Godot {

    private int hand = 0; // 0 = Left, 1 = Right
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    @Override
    public void _process(double delta) {
        String newTracker;

        // Check if our hand tracker is usable.
        newTracker = hand == 0 ? "/user/hand_tracker/left" : "/user/hand_tracker/right";
        Object handTrackerObj = call("get_tracker", newTracker);
        if (handTrackerObj != null) {
            org.godot.Godot handTracker = (org.godot.Godot) handTrackerObj;
            boolean hasTrackingData = (boolean) handTracker.getProperty("has_tracking_data");
            if (hasTrackingData) {
                String currentTracker = (String) getProperty("tracker");
                if (!newTracker.equals(currentTracker)) {
                    System.out.println("Switching to " + (hand == 0 ? "left" : "right") + " hand tracker");
                    setProperty("tracker", newTracker);
                    setProperty("pose", "default");
                }
                return;
            }
        }

        // Else fallback to our controller tracker.
        newTracker = hand == 0 ? "left_hand" : "right_hand";
        Object controllerTrackerObj = call("get_tracker", newTracker);
        if (controllerTrackerObj != null) {
            String currentTracker = (String) getProperty("tracker");
            if (!newTracker.equals(currentTracker)) {
                System.out.println("Switching to " + (hand == 0 ? "left" : "right") + " controller tracker");
                setProperty("tracker", newTracker);
            }

            org.godot.Godot controllerTracker = (org.godot.Godot) controllerTrackerObj;
            String newPose = "palm_pose";
            Object xrPoseObj = controllerTracker.call("get_pose", newPose);
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
}
