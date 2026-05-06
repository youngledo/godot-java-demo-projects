package demos.xr.openxr_hand_tracking_demo;

import org.godot.annotation.GodotClass;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "XRHandFallbackModifier3D", parent = "SkeletonModifier3D")
public class XRHandFallbackModifier3D extends org.godot.Godot {

    private String triggerAction = "trigger";
    private String gripAction = "grip";
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    @Override
    public void _process(double delta) {
        // _process_modification is called by the skeleton system.
        // In Java, we implement this as a regular call.
        processModification();
    }

    private void processModification() {
        // Get our skeleton.
        org.godot.node.Skeleton3D skeleton = (org.godot.node.Skeleton3D) call("get_skeleton");
        if (skeleton == null) return;

        // Find our parent XRNode3D.
        org.godot.Godot parent = (org.godot.Godot) call("get_parent");
        while (parent != null) {
            String className = (String) parent.call("get_class");
            if ("XRNode3D".equals(className) || "XRController3D".equals(className) || "XRCamera3D".equals(className)) {
                break;
            }
            parent = (org.godot.Godot) parent.call("get_parent");
        }
        if (parent == null) return;

        // Check if we have an active hand tracker; if so, don't need fallback.
        String parentTracker = (String) parent.getProperty("tracker");
        if (!"left_hand".equals(parentTracker) && !"right_hand".equals(parentTracker)) {
            return;
        }

        double trigger = 0.0;
        double grip = 0.0;

        // Check our tracker for trigger and grip values.
        Object trackerObj = call("get_tracker", parentTracker);
        if (trackerObj != null) {
            org.godot.Godot tracker = (org.godot.Godot) trackerObj;
            Object triggerValue = tracker.call("get_input", triggerAction);
            if (triggerValue instanceof Double) {
                trigger = (Double) triggerValue;
            } else if (triggerValue instanceof Float) {
                trigger = ((Float) triggerValue).doubleValue();
            }
            Object gripValue = tracker.call("get_input", gripAction);
            if (gripValue instanceof Double) {
                grip = (Double) gripValue;
            } else if (gripValue instanceof Float) {
                grip = ((Float) gripValue).doubleValue();
            }
        }

        // Now position bones.
        int boneCount = (int) skeleton.getBoneCount();
        double deg45rad = Math.toRadians(45.0);
        double deg20rad = Math.toRadians(20.0);
        double deg90rad = Math.toRadians(90.0);

        for (int i = 0; i < boneCount; i++) {
            org.godot.math.Transform3D t = (org.godot.math.Transform3D) skeleton.getBoneRest(i);
            String boneName = (String) skeleton.getBoneName(i);

            if ("LeftHand".equals(boneName)) {
                org.godot.math.Vector3 origin = t.getOrigin();
                t = new org.godot.math.Transform3D(t.getBasis(), origin.add(new org.godot.math.Vector3(-0.015, 0.0, 0.04)));
            } else if ("RightHand".equals(boneName)) {
                org.godot.math.Vector3 origin = t.getOrigin();
                t = new org.godot.math.Transform3D(t.getBasis(), origin.add(new org.godot.math.Vector3(0.015, 0.0, 0.04)));
            } else if ("LeftIndexDistal".equals(boneName) || "LeftIndexIntermediate".equals(boneName)
                    || "RightIndexDistal".equals(boneName) || "RightIndexIntermediate".equals(boneName)) {
                org.godot.math.Transform3D rot = new org.godot.math.Transform3D().rotated(
                        new org.godot.math.Vector3(1.0, 0.0, 0.0), deg45rad * trigger);
                t = t.multiply(rot);
            } else if ("LeftIndexProximal".equals(boneName) || "RightIndexProximal".equals(boneName)) {
                org.godot.math.Transform3D rot = new org.godot.math.Transform3D().rotated(
                        new org.godot.math.Vector3(1.0, 0.0, 0.0), deg20rad * trigger);
                t = t.multiply(rot);
            } else if (isMiddleRingLittleBone(boneName)) {
                org.godot.math.Transform3D rot = new org.godot.math.Transform3D().rotated(
                        new org.godot.math.Vector3(1.0, 0.0, 0.0), deg90rad * grip);
                t = t.multiply(rot);
            }

            skeleton.setBonePose(i, t);
        }
    }

    private boolean isMiddleRingLittleBone(String boneName) {
        return "LeftMiddleDistal".equals(boneName) || "LeftMiddleIntermediate".equals(boneName) || "LeftMiddleProximal".equals(boneName)
                || "RightMiddleDistal".equals(boneName) || "RightMiddleIntermediate".equals(boneName) || "RightMiddleProximal".equals(boneName)
                || "LeftRingDistal".equals(boneName) || "LeftRingIntermediate".equals(boneName) || "LeftRingProximal".equals(boneName)
                || "RightRingDistal".equals(boneName) || "RightRingIntermediate".equals(boneName) || "RightRingProximal".equals(boneName)
                || "LeftLittleDistal".equals(boneName) || "LeftLittleIntermediate".equals(boneName) || "LeftLittleProximal".equals(boneName)
                || "RightLittleDistal".equals(boneName) || "RightLittleIntermediate".equals(boneName) || "RightLittleProximal".equals(boneName);
    }
}
