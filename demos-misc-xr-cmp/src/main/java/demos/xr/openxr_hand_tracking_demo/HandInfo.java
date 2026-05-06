package demos.xr.openxr_hand_tracking_demo;

import org.godot.annotation.GodotClass;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "HandInfo", parent = "Node3D")
public class HandInfo extends Node3D {

    private int hand = 0; // 0 = Left, 1 = Right
    private org.godot.Godot fallbackMesh;
    private org.godot.node.Node infoLabel;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        infoLabel = getNode("Info");
    }

    @Override
    public void _process(double delta) {
        StringBuilder text = new StringBuilder();

        if (hand == 0) {
            text.append("Left hand\n");
        } else {
            text.append("Right hand\n");
        }

        String controllerTrackerName = hand == 0 ? "left_hand" : "right_hand";
        Object controllerTrackerObj = call("get_tracker", controllerTrackerName);
        if (controllerTrackerObj != null) {
            org.godot.Godot controllerTracker = (org.godot.Godot) controllerTrackerObj;
            String profile = (String) controllerTracker.getProperty("profile");
            if (profile != null) {
                profile = profile.replace("/interaction_profiles/", "").replace("/", " ");
            }
            text.append("\nProfile: ").append(profile).append("\n");

            Object poseObj = controllerTracker.call("get_pose", "palm_pose");
            org.godot.Godot xrPose = null;
            if (poseObj != null) {
                xrPose = (org.godot.Godot) poseObj;
                int confidence = (int) xrPose.getProperty("tracking_confidence");
                if (confidence != 0) { // Not XR_TRACKING_CONFIDENCE_NONE
                    text.append(" - Using palm pose\n");
                } else {
                    poseObj = controllerTracker.call("get_pose", "grip");
                    if (poseObj != null) {
                        xrPose = (org.godot.Godot) poseObj;
                        text.append(" - Using grip pose\n");
                    }
                }
            } else {
                poseObj = controllerTracker.call("get_pose", "grip");
                if (poseObj != null) {
                    xrPose = (org.godot.Godot) poseObj;
                    text.append(" - Using grip pose\n");
                }
            }

            if (xrPose != null) {
                int confidence = (int) xrPose.getProperty("tracking_confidence");
                if (confidence == 0) {
                    text.append("- No tracking data\n");
                } else if (confidence == 1) {
                    text.append("- Low confidence tracking data\n");
                } else if (confidence == 2) {
                    text.append("- High confidence tracking data\n");
                } else {
                    text.append("- Unknown tracking data ").append(confidence).append("\n");
                }
            } else {
                text.append("- No pose data\n");
            }
        } else {
            text.append("\nNo controller tracker found!\n");
        }

        String handTrackerName = hand == 0 ? "/user/hand_tracker/left" : "/user/hand_tracker/right";
        Object handTrackerObj = call("get_tracker", handTrackerName);
        if (handTrackerObj != null) {
            org.godot.Godot handTracker = (org.godot.Godot) handTrackerObj;
            text.append("\nHand tracker found\n");

            int source = (int) handTracker.getProperty("hand_tracking_source");
            if (source == 0) {
                text.append("- Source: unknown\n");
            } else if (source == 1) {
                text.append("- Source: optical hand tracking\n");
            } else if (source == 2) {
                text.append("- Source: inferred from controller\n");
            } else if (source == 3) {
                text.append("- Source: no source\n");
            } else {
                text.append("- Source: ").append(source).append("\n");
            }

            boolean hasTrackingData = (boolean) handTracker.getProperty("has_tracking_data");
            if (fallbackMesh != null) {
                fallbackMesh.setProperty("visible", !hasTrackingData);
            }
        } else {
            text.append("\nNo hand tracker found!\n");
        }

        if (infoLabel != null) {
            infoLabel.setProperty("text", text.toString());
        }
    }
}
