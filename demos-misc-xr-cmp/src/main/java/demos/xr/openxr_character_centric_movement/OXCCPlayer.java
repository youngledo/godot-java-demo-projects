package demos.xr.openxr_character_centric_movement;

import org.godot.annotation.GodotClass;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;
import org.godot.node.Node;

@GodotClass(name = "OXCCPlayer", parent = "CharacterBody3D")
public class OXCCPlayer extends CharacterBody3D {

    private double rotationSpeed = 1.0;
    private double movementSpeed = 5.0;
    private double movementAcceleration = 5.0;
    private double gravity;

    private org.godot.node.Node originNode;
    private org.godot.node.Node cameraNode;
    private org.godot.node.Node neckPositionNode;
    private org.godot.node.Node blackOut;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        gravity = (double) call("get_setting", "physics/3d/default_gravity");

        originNode = getNode("XROrigin3D");
        cameraNode = getNode("XROrigin3D/XRCamera3D");
        neckPositionNode = getNode("XROrigin3D/XRCamera3D/Neck");
        blackOut = getNode("XROrigin3D/XRCamera3D/BlackOut");
    }

    @org.godot.annotation.GodotMethod
    public void recenter() {
        org.godot.Godot xrInterface = (org.godot.Godot) call("find_interface", "OpenXR");
        if (xrInterface == null) return;

        int playAreaMode = (int) xrInterface.call("get_play_area_mode");
        if (playAreaMode == 1) { // XR_PLAY_AREA_SITTING
            // Sitting play space is not suitable for this setup.
        } else if (playAreaMode == 2) { // XR_PLAY_AREA_ROOMSCALE
            // This is already handled by the headset.
        } else {
            // Use Godot's own logic.
            call("center_on_hmd", 1, true); // RESET_BUT_KEEP_TILT
        }

        // Get head tracker.
        Object headTracker = call("get_tracker", "head");
        if (headTracker == null || !(headTracker instanceof org.godot.Godot)) return;
        org.godot.Godot headTrackerNode = (org.godot.Godot) headTracker;

        Object poseObj = headTrackerNode.call("get_pose", "default");
        if (poseObj == null) return;
        org.godot.Godot xrPose = (org.godot.Godot) poseObj;
        Transform3D headTransform = (Transform3D) xrPose.call("get_adjusted_transform");

        // Get neck transform in XROrigin3D space.
        Transform3D neckTransform = (Transform3D) neckPositionNode.call("get_transform");
        neckTransform = neckTransform.multiply(headTransform);

        // Reset our XROrigin transform.
        Transform3D newOriginTransform = new Transform3D();
        newOriginTransform = new Transform3D(
                new Basis(1, 0, 0, 0, 1, 0, 0, 0, 1),
                new Vector3(-neckTransform.getOrigin().x, 0.0, -neckTransform.getOrigin().z));
        originNode.call("set_transform", newOriginTransform);

        // Reset character orientation.
        Transform3D characterTransform = (Transform3D) getProperty("transform");
        characterTransform = new Transform3D(new Basis(), characterTransform.getOrigin());
        setProperty("transform", characterTransform);
    }

    private Vector2 getMovementInput() {
        org.godot.node.Node leftHand = getNode("XROrigin3D/LeftHand");
        org.godot.node.Node rightHand = getNode("XROrigin3D/RightHand");

        Vector2 movement = new Vector2(0, 0);
        if (leftHand != null) {
            Object leftVec = leftHand.call("get_vector2", "move");
            if (leftVec instanceof Vector2) {
                movement = movement.add((Vector2) leftVec);
            }
        }
        if (rightHand != null) {
            Object rightVec = rightHand.call("get_vector2", "move");
            if (rightVec instanceof Vector2) {
                movement = movement.add((Vector2) rightVec);
            }
        }
        return movement;
    }

    private boolean processOnPhysicalMovement(double delta) {
        Vector3 currentVelocity = (Vector3) getProperty("velocity");

        // Rotate the player to face the same way our real player is.
        Transform3D originTransform = (Transform3D) originNode.call("get_transform");
        Transform3D cameraTransform = (Transform3D) cameraNode.call("get_transform");
        Transform3D combinedBasisTransform = originTransform.multiply(cameraTransform);
        Basis cameraBasis = combinedBasisTransform.getBasis();

        Vector3 forward = new Vector3(cameraBasis.zx, cameraBasis.zy, cameraBasis.zz);
        Vector2 forward2d = new Vector2(forward.x, forward.z);
        Vector2 target2d = new Vector2(0.0, 1.0);
        double angle = forward2d.angleTo(target2d);

        // Rotate our character body.
        Transform3D selfTransform = (Transform3D) getProperty("transform");
        Basis selfBasis = selfTransform.getBasis();
        selfBasis = Basis.fromAxisAngle(Vector3.UP, angle).multiply(selfBasis);
        selfTransform = new Transform3D(selfBasis, selfTransform.getOrigin());
        setProperty("transform", selfTransform);

        // Reverse this rotation on origin node.
        Basis originBasis = originTransform.getBasis();
        Transform3D rotation = Transform3D.rotated(Vector3.UP, -angle);
        originTransform = rotation.multiply(originTransform);
        originNode.call("set_transform", originTransform);

        // Now move our player body to the right location.
        Vector3 orgPlayerBody = (Vector3) getProperty("global_position");
        Transform3D neckTransformObj = (Transform3D) neckPositionNode.call("get_transform");
        Vector3 neckOrigin = neckTransformObj.getOrigin();

        // Combine transforms to get player body location.
        Transform3D camTf = (Transform3D) cameraNode.call("get_transform");
        Vector3 playerBodyLocation = camTf.apply(neckOrigin);
        playerBodyLocation = new Vector3(playerBodyLocation.x, 0.0, playerBodyLocation.z);
        Transform3D originTf = (Transform3D) originNode.call("get_transform");
        Vector3 globalOrigin = originTf.apply(playerBodyLocation);

        Vector3 velocity = globalOrigin.sub(orgPlayerBody).div(delta);
        setProperty("velocity", velocity);
        moveAndSlide();

        // Move XROrigin back.
        Vector3 newGlobalPos = (Vector3) getProperty("global_position");
        Vector3 deltaMovement = newGlobalPos.sub(orgPlayerBody);
        Vector3 originGlobalPos = (Vector3) originNode.call("get_global_position");
        originNode.call("set_global_position", originGlobalPos.sub(deltaMovement));

        // Negate height change in local space.
        Transform3D updatedOriginTransform = (Transform3D) originNode.call("get_transform");
        updatedOriginTransform = new Transform3D(
                updatedOriginTransform.getBasis(),
                new Vector3(updatedOriginTransform.getOrigin().x, 0.0, updatedOriginTransform.getOrigin().z));
        originNode.call("set_transform", updatedOriginTransform);

        // Restore velocity.
        setProperty("velocity", currentVelocity);

        // Check if we managed to move where we wanted to.
        double locationOffset = globalOrigin.sub(newGlobalPos).length();
        if (locationOffset > 0.1) {
            double fadeValue = Math.max(0.0, Math.min(1.0, (locationOffset - 0.1) / 0.1));
            if (blackOut != null) blackOut.set("fade", fadeValue);
            return true;
        } else {
            if (blackOut != null) blackOut.set("fade", 0.0);
            return false;
        }
    }

    private void processMovementOnInput(boolean isColliding, double delta) {
        if (!isColliding) {
            Vector2 movementInput = getMovementInput();

            // Handle rotation.
            double rotY = (double) getProperty("rotation:y");
            rotY = rotY + (-movementInput.x * delta * rotationSpeed);
            setProperty("rotation:y", rotY);

            // Handle forward/backward movement.
            Transform3D globalTf = (Transform3D) call("get_global_transform");
            Basis globalBasis = globalTf.getBasis();
            Vector3 direction = new Vector3(
                    globalBasis.xx * 0.0 + globalBasis.yx * 0.0 + globalBasis.zx * (-movementInput.y),
                    globalBasis.xy * 0.0 + globalBasis.yy * 0.0 + globalBasis.zy * (-movementInput.y),
                    globalBasis.xz * 0.0 + globalBasis.yz * 0.0 + globalBasis.zz * (-movementInput.y));
            direction = direction.mul(movementSpeed);

            Vector3 velocity = (Vector3) getProperty("velocity");
            if (direction.length() > 0.001) {
                velocity = new Vector3(
                        moveToward(velocity.x, direction.x, delta * movementAcceleration),
                        velocity.y,
                        moveToward(velocity.z, direction.z, delta * movementAcceleration));
            } else {
                velocity = new Vector3(
                        moveToward(velocity.x, 0, delta * movementAcceleration),
                        velocity.y,
                        moveToward(velocity.z, 0, delta * movementAcceleration));
            }
            setProperty("velocity", velocity);
        }

        // Always handle gravity.
        Vector3 velocity = (Vector3) getProperty("velocity");
        velocity = new Vector3(velocity.x, velocity.y - gravity * delta, velocity.z);
        setProperty("velocity", velocity);
        moveAndSlide();
    }

    private double moveToward(double current, double target, double maxDelta) {
        if (Math.abs(target - current) <= maxDelta) {
            return target;
        }
        return current + Math.signum(target - current) * maxDelta;
    }

    @Override
    public void _physicsProcess(double delta) {
        boolean isColliding = processOnPhysicalMovement(delta);
        processMovementOnInput(isColliding, delta);
    }
}
