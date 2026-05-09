package demos.xr.openxr_hand_tracking_demo;

import org.godot.annotation.GodotClass;
import org.godot.math.Transform3D;
import org.godot.math.Vector3;
import org.godot.node.Node;
import org.godot.node.Skeleton3D;
import org.godot.node.SkeletonModifier3D;
import org.godot.node.XRNode3D;
import org.godot.node.XRPositionalTracker;
import org.godot.node.XRTracker;
import org.godot.singleton.XRServer;

@GodotClass(name = "XRHandFallbackModifier3D", parent = "SkeletonModifier3D")
public class XRHandFallbackModifier3D extends SkeletonModifier3D {

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
        processModification();
    }

    private void processModification() {
        Skeleton3D skeleton = getSkeleton();
        if (skeleton == null) return;

        Node parent = getParent();
        while (parent != null && !(parent instanceof XRNode3D)) {
            parent = parent.getParent();
        }
        if (!(parent instanceof XRNode3D xrNode)) return;

        String parentTracker = xrNode.getTracker();
        if (!"left_hand".equals(parentTracker) && !"right_hand".equals(parentTracker)) {
            return;
        }

        double trigger = 0.0;
        double grip = 0.0;

        XRTracker tracker = XRServer.singleton().getTracker(parentTracker);
        if (tracker instanceof XRPositionalTracker positionalTracker) {
            trigger = inputAsDouble(positionalTracker.getInput(triggerAction));
            grip = inputAsDouble(positionalTracker.getInput(gripAction));
        }

        int boneCount = skeleton.getBoneCount();
        double deg45rad = Math.toRadians(45.0);
        double deg20rad = Math.toRadians(20.0);
        double deg90rad = Math.toRadians(90.0);

        for (int i = 0; i < boneCount; i++) {
            Transform3D transform = skeleton.getBoneRest(i);
            String boneName = skeleton.getBoneName(i);

            if ("LeftHand".equals(boneName)) {
                Vector3 origin = transform.getOrigin();
                transform = new Transform3D(transform.getBasis(), origin.add(new Vector3(-0.015, 0.0, 0.04)));
            } else if ("RightHand".equals(boneName)) {
                Vector3 origin = transform.getOrigin();
                transform = new Transform3D(transform.getBasis(), origin.add(new Vector3(0.015, 0.0, 0.04)));
            } else if ("LeftIndexDistal".equals(boneName) || "LeftIndexIntermediate".equals(boneName)
                    || "RightIndexDistal".equals(boneName) || "RightIndexIntermediate".equals(boneName)) {
                Transform3D rotation = new Transform3D().rotated(new Vector3(1.0, 0.0, 0.0), deg45rad * trigger);
                transform = transform.multiply(rotation);
            } else if ("LeftIndexProximal".equals(boneName) || "RightIndexProximal".equals(boneName)) {
                Transform3D rotation = new Transform3D().rotated(new Vector3(1.0, 0.0, 0.0), deg20rad * trigger);
                transform = transform.multiply(rotation);
            } else if (isMiddleRingLittleBone(boneName)) {
                Transform3D rotation = new Transform3D().rotated(new Vector3(1.0, 0.0, 0.0), deg90rad * grip);
                transform = transform.multiply(rotation);
            }

            skeleton.setBonePose(i, transform);
        }
    }

    private double inputAsDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
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
