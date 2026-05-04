package demos.xr.openxr_origin_centric_movement;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Node3D;

@GodotClass(name = "OXOCPlayer", parent = "XROrigin3D")
public class OXOCPlayer extends org.godot.Godot {

    private double rotationSpeed = 1.0;
    private double movementSpeed = 5.0;
    private double movementAcceleration = 5.0;
    private double gravity;

    private org.godot.Godot characterBody;
    private org.godot.Godot cameraNode;
    private org.godot.Godot neckPositionNode;
    private org.godot.Godot blackOut;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        gravity = (double) call("get_setting", "physics/3d/default_gravity");

        characterBody = (org.godot.Godot) call("get_node", "CharacterBody3D");
        cameraNode = (org.godot.Godot) call("get_node", "XRCamera3D");
        neckPositionNode = (org.godot.Godot) call("get_node", "XRCamera3D/Neck");
        blackOut = (org.godot.Godot) call("get_node", "XRCamera3D/BlackOut");
    }

    @GodotMethod
    public void recenter() {
        if (characterBody == null || cameraNode == null || neckPositionNode == null) return;

        // Calculate where our camera should be.
        org.godot.math.Transform3D charGlobalTransform = (org.godot.math.Transform3D) characterBody.call("get_global_transform");
        org.godot.math.Transform3D newCameraTransform = charGlobalTransform;

        // Set to the height of our neck joint.
        org.godot.math.Vector3 neckGlobalPos = (org.godot.math.Vector3) neckPositionNode.call("get_global_position");
        newCameraTransform = new org.godot.math.Transform3D(
                newCameraTransform.getBasis(),
                new org.godot.math.Vector3(newCameraTransform.getOrigin().x, neckGlobalPos.y, newCameraTransform.getOrigin().z));

        // Apply neck inverse.
        org.godot.math.Transform3D neckTransform = (org.godot.math.Transform3D) neckPositionNode.call("get_transform");
        newCameraTransform = newCameraTransform.multiply(neckTransform.inverse());

        // Remove tilt from camera transform.
        org.godot.math.Transform3D cameraTransform = (org.godot.math.Transform3D) cameraNode.call("get_transform");
        org.godot.math.Vector3 forwardDir = new org.godot.math.Vector3(
                cameraTransform.getBasis().zx,
                cameraTransform.getBasis().zy,
                cameraTransform.getBasis().zz);
        forwardDir.y = 0.0;
        double len = forwardDir.length();
        if (len > 0.001) {
            forwardDir = forwardDir.div(len);
        }

        // looking_at - compute a basis that looks in forwardDir using cross products
        org.godot.math.Vector3 forward = forwardDir.normalized();
        org.godot.math.Vector3 right = Vector3.UP.cross(forward).normalized();
        org.godot.math.Vector3 up = forward.cross(right).normalized();
        Basis lookingBasis = new Basis(
                right.x, up.x, forward.x,
                right.y, up.y, forward.y,
                right.z, up.z, forward.z);
        cameraTransform = new org.godot.math.Transform3D(lookingBasis, cameraTransform.getOrigin());

        // Update our XR location.
        org.godot.math.Transform3D newGlobalTransform = newCameraTransform.multiply(cameraTransform.inverse());
        call("set_global_transform", newGlobalTransform);

        // Recenter character body.
        characterBody.call("set_transform", new org.godot.math.Transform3D());
    }

    private org.godot.math.Vector2 getMovementInput() {
        org.godot.math.Vector2 movement = new org.godot.math.Vector2(0, 0);

        org.godot.Godot leftHand = (org.godot.Godot) call("get_node", "LeftHand");
        org.godot.Godot rightHand = (org.godot.Godot) call("get_node", "RightHand");

        if (leftHand != null) {
            Object leftVec = leftHand.call("get_vector2", "move");
            if (leftVec instanceof org.godot.math.Vector2) {
                movement = movement.add((org.godot.math.Vector2) leftVec);
            }
        }
        if (rightHand != null) {
            Object rightVec = rightHand.call("get_vector2", "move");
            if (rightVec instanceof org.godot.math.Vector2) {
                movement = movement.add((org.godot.math.Vector2) rightVec);
            }
        }
        return movement;
    }

    private boolean processOnPhysicalMovement(double delta) {
        if (characterBody == null || cameraNode == null) return false;

        org.godot.math.Vector3 currentVelocity = (org.godot.math.Vector3) characterBody.getProperty("velocity");

        org.godot.math.Vector3 orgPlayerBody = (org.godot.math.Vector3) characterBody.call("get_global_position");

        // Determine where our player body should be.
        org.godot.math.Transform3D cameraTransform = (org.godot.math.Transform3D) cameraNode.call("get_transform");
        org.godot.math.Transform3D neckTransform = (org.godot.math.Transform3D) neckPositionNode.call("get_transform");
        org.godot.math.Vector3 playerBodyLocation = cameraTransform.apply(neckTransform.getOrigin());
        playerBodyLocation = new org.godot.math.Vector3(playerBodyLocation.x, 0.0, playerBodyLocation.z);

        org.godot.math.Transform3D globalTransform = (org.godot.math.Transform3D) call("get_global_transform");
        playerBodyLocation = globalTransform.apply(playerBodyLocation);

        // Attempt to move our character.
        org.godot.math.Vector3 velocity = playerBodyLocation.sub(orgPlayerBody).div(delta);
        characterBody.setProperty("velocity", velocity);
        characterBody.call("move_and_slide");

        // Set back to current value.
        characterBody.setProperty("velocity", currentVelocity);

        // Check if we managed to move all the way.
        org.godot.math.Vector3 newCharPos = (org.godot.math.Vector3) characterBody.call("get_global_position");
        org.godot.math.Vector3 movementLeft = playerBodyLocation.sub(newCharPos);
        movementLeft = new org.godot.math.Vector3(movementLeft.x, 0.0, movementLeft.z);

        double locationOffset = movementLeft.length();
        if (locationOffset > 0.1) {
            double fadeValue = Math.max(0.0, Math.min(1.0, (locationOffset - 0.1) / 0.1));
            if (blackOut != null) blackOut.call("set", "fade", fadeValue);
            return true;
        } else {
            if (blackOut != null) blackOut.call("set", "fade", 0.0);
            return false;
        }
    }

    private void copyPlayerRotationToCharacterBody() {
        org.godot.math.Vector3 cameraForward = (org.godot.math.Vector3) cameraNode.call("get_global_transform");
        if (!(cameraForward instanceof org.godot.math.Vector3)) {
            Object tf = cameraNode.call("get_global_transform");
            if (tf instanceof org.godot.math.Transform3D) {
                org.godot.math.Transform3D cameraTf = (org.godot.math.Transform3D) tf;
                org.godot.math.Basis cameraBasis = cameraTf.getBasis();
                org.godot.math.Vector3 forward = new org.godot.math.Vector3(-cameraBasis.zx, -cameraBasis.zy, -cameraBasis.zz);
                org.godot.math.Vector3 bodyForward = new org.godot.math.Vector3(forward.x, 0.0, forward.z);
                double len = bodyForward.length();
                if (len > 0.001) {
                    bodyForward = bodyForward.div(len);
                }

                // Compute looking-at basis for bodyForward direction
                org.godot.math.Vector3 fwd = bodyForward.normalized();
                org.godot.math.Vector3 r = Vector3.UP.cross(fwd).normalized();
                org.godot.math.Vector3 u = fwd.cross(r).normalized();
                Basis lookingBasis = new Basis(
                        r.x, u.x, fwd.x,
                        r.y, u.y, fwd.y,
                        r.z, u.z, fwd.z);
                org.godot.math.Vector3 charPos = (org.godot.math.Vector3) characterBody.call("get_global_position");
                characterBody.call("set_global_transform", new org.godot.math.Transform3D(lookingBasis, charPos));
            }
        }
    }

    private void processMovementOnInput(boolean isColliding, double delta) {
        if (characterBody == null) return;

        org.godot.math.Vector3 orgPlayerBody = (org.godot.math.Vector3) characterBody.call("get_global_position");

        if (!isColliding) {
            org.godot.math.Vector2 movementInput = getMovementInput();

            // Handle rotation - rotate the origin around the player.
            org.godot.math.Vector3 globalOrigin = (org.godot.math.Vector3) call("get_global_position");
            org.godot.math.Vector3 charGlobalPos = (org.godot.math.Vector3) characterBody.call("get_global_position");
            org.godot.math.Vector3 playerPosition = charGlobalPos.sub(globalOrigin);

            org.godot.math.Transform3D t1 = new org.godot.math.Transform3D(new Basis(), playerPosition.mul(-1));
            org.godot.math.Transform3D t2 = new org.godot.math.Transform3D(new Basis(), playerPosition);
            org.godot.math.Transform3D rot = org.godot.math.Transform3D.rotated(
                    new org.godot.math.Vector3(0.0, 1.0, 0.0), -movementInput.x * delta * rotationSpeed);

            org.godot.math.Transform3D currentGlobal = (org.godot.math.Transform3D) call("get_global_transform");
            org.godot.math.Transform3D newGlobal = currentGlobal.multiply(t2).multiply(rot).multiply(t1);
            // orthonormalized
            // Orthonormalize: normalize the basis axes
            org.godot.math.Basis nb = newGlobal.getBasis();
            org.godot.math.Vector3 col0 = new org.godot.math.Vector3(nb.xx, nb.yx, nb.zx).normalized();
            org.godot.math.Vector3 col1 = col0.cross(new org.godot.math.Vector3(nb.xz, nb.yz, nb.zz).normalized()).normalized();
            org.godot.math.Vector3 col2 = col0.cross(col1).normalized();
            org.godot.math.Basis orthoBasis = new org.godot.math.Basis(
                    col0.x, col1.x, col2.x,
                    col0.y, col1.y, col2.y,
                    col0.z, col1.z, col2.z);
            call("set_global_transform", new org.godot.math.Transform3D(orthoBasis, newGlobal.getOrigin()));

            // Ensure player body is facing the correct way.
            copyPlayerRotationToCharacterBody();

            // Handle forward/backwards movement.
            org.godot.math.Transform3D charGlobalTransform = (org.godot.math.Transform3D) characterBody.call("get_global_transform");
            org.godot.math.Basis charBasis = charGlobalTransform.getBasis();
            org.godot.math.Vector3 direction = new org.godot.math.Vector3(
                    charBasis.zx * (-movementInput.y),
                    charBasis.zy * (-movementInput.y),
                    charBasis.zz * (-movementInput.y));
            direction = direction.mul(movementSpeed);

            org.godot.math.Vector3 charVelocity = (org.godot.math.Vector3) characterBody.getProperty("velocity");
            if (direction.length() > 0.001) {
                charVelocity = new org.godot.math.Vector3(
                        moveToward(charVelocity.x, direction.x, delta * movementAcceleration),
                        charVelocity.y,
                        moveToward(charVelocity.z, direction.z, delta * movementAcceleration));
            } else {
                charVelocity = new org.godot.math.Vector3(
                        moveToward(charVelocity.x, 0, delta * movementAcceleration),
                        charVelocity.y,
                        moveToward(charVelocity.z, 0, delta * movementAcceleration));
            }
            characterBody.setProperty("velocity", charVelocity);
        }

        // Always handle gravity.
        org.godot.math.Vector3 charVelocity = (org.godot.math.Vector3) characterBody.getProperty("velocity");
        charVelocity = new org.godot.math.Vector3(charVelocity.x, charVelocity.y - gravity * delta, charVelocity.z);
        characterBody.setProperty("velocity", charVelocity);

        // Attempt to move our player.
        characterBody.call("move_and_slide");

        // Apply actual movement to origin.
        org.godot.math.Vector3 newCharPos = (org.godot.math.Vector3) characterBody.call("get_global_position");
        org.godot.math.Vector3 deltaMovement = newCharPos.sub(orgPlayerBody);
        org.godot.math.Vector3 originGlobalPos = (org.godot.math.Vector3) call("get_global_position");
        call("set_global_position", originGlobalPos.add(deltaMovement));
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
