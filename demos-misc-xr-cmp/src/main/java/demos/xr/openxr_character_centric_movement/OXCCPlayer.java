package demos.xr.openxr_character_centric_movement;

import demos.xr.openxr_character_centric_movement.objects.BlackOut;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;
import org.godot.node.Node3D;
import org.godot.node.XRController3D;
import org.godot.node.XRInterface;
import org.godot.node.XRPose;
import org.godot.node.XRPositionalTracker;
import org.godot.node.XRTracker;
import org.godot.node.XROrigin3D;
import org.godot.singleton.ProjectSettings;
import org.godot.singleton.XRServer;

@GodotClass(name = "OXCCPlayer", parent = "CharacterBody3D")
public class OXCCPlayer extends CharacterBody3D {

    private double rotationSpeed = 1.0;
    private double movementSpeed = 5.0;
    private double movementAcceleration = 5.0;
    private double gravity;

    private XROrigin3D originNode;
    private Node3D cameraNode;
    private Node3D neckPositionNode;
    private BlackOut blackOut;
    private XRController3D leftHand;
    private XRController3D rightHand;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object gravitySetting = ProjectSettings.singleton().getSetting("physics/3d/default_gravity");
        gravity = gravitySetting instanceof Number number ? number.doubleValue() : 9.8;

        originNode = getNodeAs("XROrigin3D", XROrigin3D.class);
        cameraNode = getNodeAs("XROrigin3D/XRCamera3D", Node3D.class);
        neckPositionNode = getNodeAs("XROrigin3D/XRCamera3D/Neck", Node3D.class);
        blackOut = getNodeAs("XROrigin3D/XRCamera3D/BlackOut", BlackOut.class);
        leftHand = getNodeAs("XROrigin3D/LeftHand", XRController3D.class);
        rightHand = getNodeAs("XROrigin3D/RightHand", XRController3D.class);
    }

    @GodotMethod
    public void recenter() {
        if (originNode == null || neckPositionNode == null) return;

        XRServer xrServer = XRServer.singleton();
        XRInterface xrInterface = xrServer.findInterface("OpenXR");
        if (xrInterface == null) return;

        XRInterface.PlayAreaMode playAreaMode = xrInterface.getPlayAreaMode();
        if (playAreaMode == XRInterface.PlayAreaMode.XR_PLAY_AREA_SITTING) {
            // Sitting play space is not suitable for this setup.
        } else if (playAreaMode == XRInterface.PlayAreaMode.XR_PLAY_AREA_ROOMSCALE) {
            // This is already handled by the headset.
        } else {
            // Use Godot's own logic.
            xrServer.centerOnHmd(XRServer.RotationMode.RESET_BUT_KEEP_TILT, true);
        }

        // Get head tracker.
        XRTracker headTracker = xrServer.getTracker("head");
        if (!(headTracker instanceof XRPositionalTracker headTrackerNode)) return;

        XRPose xrPose = headTrackerNode.getPose("default");
        if (xrPose == null) return;
        Transform3D headTransform = xrPose.getAdjustedTransform();

        // Get neck transform in XROrigin3D space.
        Transform3D neckTransform = neckPositionNode.getTransform();
        neckTransform = neckTransform.multiply(headTransform);

        // Reset our XROrigin transform.
        Transform3D newOriginTransform = new Transform3D(
                new Basis(1, 0, 0, 0, 1, 0, 0, 0, 1),
                new Vector3(-neckTransform.getOrigin().x, 0.0, -neckTransform.getOrigin().z));
        originNode.setTransform(newOriginTransform);

        // Reset character orientation.
        Transform3D characterTransform = getTransform();
        characterTransform = new Transform3D(new Basis(), characterTransform.getOrigin());
        setTransform(characterTransform);
    }

    private Vector2 getMovementInput() {
        Vector2 movement = new Vector2(0, 0);
        if (leftHand != null) {
            movement = movement.add(leftHand.getVector2("move"));
        }
        if (rightHand != null) {
            movement = movement.add(rightHand.getVector2("move"));
        }
        return movement;
    }

    private boolean processOnPhysicalMovement(double delta) {
        if (originNode == null || cameraNode == null || neckPositionNode == null) return false;

        Vector3 currentVelocity = getVelocity();

        // Rotate the player to face the same way our real player is.
        Transform3D originTransform = originNode.getTransform();
        Transform3D cameraTransform = cameraNode.getTransform();
        Transform3D combinedBasisTransform = originTransform.multiply(cameraTransform);
        Basis cameraBasis = combinedBasisTransform.getBasis();

        Vector3 forward = new Vector3(cameraBasis.zx, cameraBasis.zy, cameraBasis.zz);
        Vector2 forward2d = new Vector2(forward.x, forward.z);
        Vector2 target2d = new Vector2(0.0, 1.0);
        double angle = forward2d.angleTo(target2d);

        // Rotate our character body.
        Transform3D selfTransform = getTransform();
        Basis selfBasis = selfTransform.getBasis();
        selfBasis = Basis.fromAxisAngle(Vector3.UP, angle).multiply(selfBasis);
        selfTransform = new Transform3D(selfBasis, selfTransform.getOrigin());
        setTransform(selfTransform);

        // Reverse this rotation on origin node.
        Transform3D rotation = Transform3D.rotated(Vector3.UP, -angle);
        originTransform = rotation.multiply(originTransform);
        originNode.setTransform(originTransform);

        // Now move our player body to the right location.
        Vector3 orgPlayerBody = getGlobalPosition();
        Transform3D neckTransformObj = neckPositionNode.getTransform();
        Vector3 neckOrigin = neckTransformObj.getOrigin();

        // Combine transforms to get player body location.
        Transform3D camTf = cameraNode.getTransform();
        Vector3 playerBodyLocation = camTf.apply(neckOrigin);
        playerBodyLocation = new Vector3(playerBodyLocation.x, 0.0, playerBodyLocation.z);
        Transform3D originTf = originNode.getTransform();
        Vector3 globalOrigin = originTf.apply(playerBodyLocation);

        Vector3 velocity = globalOrigin.sub(orgPlayerBody).div(delta);
        setVelocity(velocity);
        moveAndSlide();

        // Move XROrigin back.
        Vector3 newGlobalPos = getGlobalPosition();
        Vector3 deltaMovement = newGlobalPos.sub(orgPlayerBody);
        Vector3 originGlobalPos = originNode.getGlobalPosition();
        originNode.setGlobalPosition(originGlobalPos.sub(deltaMovement));

        // Negate height change in local space.
        Transform3D updatedOriginTransform = originNode.getTransform();
        updatedOriginTransform = new Transform3D(
                updatedOriginTransform.getBasis(),
                new Vector3(updatedOriginTransform.getOrigin().x, 0.0, updatedOriginTransform.getOrigin().z));
        originNode.setTransform(updatedOriginTransform);

        // Restore velocity.
        setVelocity(currentVelocity);

        // Check if we managed to move where we wanted to.
        double locationOffset = globalOrigin.sub(newGlobalPos).length();
        if (locationOffset > 0.1) {
            double fadeValue = Math.max(0.0, Math.min(1.0, (locationOffset - 0.1) / 0.1));
            if (blackOut != null) blackOut.setFade(fadeValue);
            return true;
        } else {
            if (blackOut != null) blackOut.setFade(0.0);
            return false;
        }
    }

    private void processMovementOnInput(boolean isColliding, double delta) {
        if (!isColliding) {
            Vector2 movementInput = getMovementInput();

            // Handle rotation.
            Vector3 rotation = getRotation();
            setRotation(new Vector3(rotation.x, rotation.y + (-movementInput.x * delta * rotationSpeed), rotation.z));

            // Handle forward/backward movement.
            Transform3D globalTf = getGlobalTransform();
            Basis globalBasis = globalTf.getBasis();
            Vector3 direction = new Vector3(
                    globalBasis.xx * 0.0 + globalBasis.yx * 0.0 + globalBasis.zx * (-movementInput.y),
                    globalBasis.xy * 0.0 + globalBasis.yy * 0.0 + globalBasis.zy * (-movementInput.y),
                    globalBasis.xz * 0.0 + globalBasis.yz * 0.0 + globalBasis.zz * (-movementInput.y));
            direction = direction.mul(movementSpeed);

            Vector3 velocity = getVelocity();
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
            setVelocity(velocity);
        }

        // Always handle gravity.
        Vector3 velocity = getVelocity();
        velocity = new Vector3(velocity.x, velocity.y - gravity * delta, velocity.z);
        setVelocity(velocity);
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
