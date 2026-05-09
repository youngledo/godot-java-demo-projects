package demos.xr.openxr_hand_tracking_demo;

import org.godot.annotation.GodotClass;
import org.godot.node.Label3D;
import org.godot.node.Node3D;
import org.godot.node.XRPose;
import org.godot.node.XRPositionalTracker;
import org.godot.node.XRTracker;
import org.godot.singleton.XRServer;

@GodotClass(name = "HandInfo", parent = "Node3D")
public class HandInfo extends Node3D {

    private int hand = 0;
    private Node3D fallbackMesh;
    private Label3D infoLabel;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        infoLabel = getNodeAs("Info", Label3D.class);
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
        XRTracker controllerTracker = XRServer.singleton().getTracker(controllerTrackerName);
        if (controllerTracker instanceof XRPositionalTracker positionalTracker) {
            String profile = positionalTracker.getProfile();
            if (profile != null) {
                profile = profile.replace("/interaction_profiles/", "").replace("/", " ");
            }
            text.append("\nProfile: ").append(profile).append("\n");

            XRPose xrPose = positionalTracker.getPose("palm_pose");
            if (xrPose != null) {
                if (xrPose.getTrackingConfidence() != 0) {
                    text.append(" - Using palm pose\n");
                } else {
                    xrPose = positionalTracker.getPose("grip");
                    if (xrPose != null) {
                        text.append(" - Using grip pose\n");
                    }
                }
            } else {
                xrPose = positionalTracker.getPose("grip");
                if (xrPose != null) {
                    text.append(" - Using grip pose\n");
                }
            }

            if (xrPose != null) {
                long confidence = xrPose.getTrackingConfidence();
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
        XRTracker handTracker = XRServer.singleton().getTracker(handTrackerName);
        if (handTracker != null) {
            text.append("\nHand tracker found\n");

            Object sourceValue = handTracker.getProperty("hand_tracking_source");
            long source = sourceValue instanceof Number number ? number.longValue() : 0;
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

            if (fallbackMesh != null) {
                fallbackMesh.setVisible(!Boolean.TRUE.equals(handTracker.getProperty("has_tracking_data")));
            }
        } else {
            text.append("\nNo hand tracker found!\n");
        }

        if (infoLabel != null) {
            infoLabel.setText(text.toString());
        }
    }
}
