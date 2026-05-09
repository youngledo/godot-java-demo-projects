package demos.xr.openxr_origin_centric_movement;

import demos.xr.openxr_origin_centric_movement.objects.OXOCBlackOut;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.CharacterBody3D;
import org.godot.node.Node3D;
import org.godot.node.XRController3D;
import org.godot.node.XROrigin3D;
import org.godot.singleton.ProjectSettings;

@GodotClass(name = "OXOCPlayer", parent = "XROrigin3D")
public class OXOCPlayer extends XROrigin3D {

    private double rotationSpeed = 1.0;
    private double movementSpeed = 5.0;
    private double movementAcceleration = 5.0;
    private double gravity;

    private CharacterBody3D characterBody;
    private Node3D cameraNode;
    private Node3D neckPositionNode;
    private OXOCBlackOut blackOut;
    private XRController3D leftHand;
    private XRController3D rightHand;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object gravitySetting = ProjectSettings.singleton().getSetting("physics/3d/default_gravity");
        gravity = gravitySetting instanceof Number number ? number.doubleValue() : 9.8;

        characterBody = getNodeAs("CharacterBody3D", CharacterBody3D.class);
        cameraNode = getNodeAs("XRCamera3D", Node3D.class);
        neckPositionNode = getNodeAs("XRCamera3D/Neck", Node3D.class);
        blackOut = getNodeAs("XRCamera3D/BlackOut", OXOCBlackOut.class);
        leftHand = getNodeAs("LeftHand", XRController3D.class);
        rightHand = getNodeAs("RightHand", XRController3D.class);
    }

    @GodotMethod
    public void recenter() {
        if (characterBody == null || cameraNode == null || neckPositionNode == null) return;

        // Calculate where our camera should be.
        Transform3D charGlobalTransform = characterBody.getGlobalTransform();
        Transform3D newCameraTransform = charGlobalTransform;

        // Set to the height of our neck joint.
        Vector3 neckGlobalPos = neckPositionNode.getGlobalPosition();
        newCameraTransform = new Transform3D(
                newCameraTransform.getBasis(),
                new Vector3(newCameraTransform.getOrigin().x, neckGlobalPos.y, newCameraTransform.getOrigin().z));

        // Apply neck inverse.
        Transform3D neckTransform = neckPositionNode.getTransform();
        newCameraTransform = newCameraTransform.multiply(neckTransform.inverse());

        // Remove tilt from camera transform.
        Transform3D cameraTransform = cameraNode.getTransform();
        Vector3 forwardDir = new Vector3(
                cameraTransform.getBasis().zx,
                cameraTransform.getBasis().zy,
                cameraTransform.getBasis().zz);
        forwardDir.y = 0.0;
        double len = forwardDir.length();
        if (len > 0.001) {
            forwardDir = forwardDir.div(len);
        }

        // looking_at - compute a basis that looks in forwardDir using cross products
        Vector3 forward = forwardDir.normalized();
        Vector3 right = Vector3.UP.cross(forward).normalized();
        Vector3 up = forward.cross(right).normalized();
        Basis lookingBasis = new Basis(
                right.x, up.x, forward.x,
                right.y, up.y, forward.y,
                right.z, up.z, forward.z);
        cameraTransform = new Transform3D(lookingBasis, cameraTransform.getOrigin());

        // Update our XR location.
        Transform3D newGlobalTransform = newCameraTransform.multiply(cameraTransform.inverse());
        setGlobalTransform(newGlobalTransform);

        // Recenter character body.
        characterBody.setTransform(new Transform3D());
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
        if (characterBody == null || cameraNode == null || neckPositionNode == null) return false;

        Vector3 currentVelocity = characterBody.getVelocity();
        Vector3 orgPlayerBody = characterBody.getGlobalPosition();

        // Determine where our player body should be.
        Transform3D cameraTransform = cameraNode.getTransform();
        Transform3D neckTransform = neckPositionNode.getTransform();
        Vector3 playerBodyLocation = cameraTransform.apply(neckTransform.getOrigin());
        playerBodyLocation = new Vector3(playerBodyLocation.x, 0.0, playerBodyLocation.z);

        Transform3D globalTransform = getGlobalTransform();
        playerBodyLocation = globalTransform.apply(playerBodyLocation);

        // Attempt to move our character.
        Vector3 velocity = playerBodyLocation.sub(orgPlayerBody).div(delta);
        characterBody.setVelocity(velocity);
        characterBody.moveAndSlide();

        // Set back to current value.
        characterBody.setVelocity(currentVelocity);

        // Check if we managed to move all the way.
        Vector3 newCharPos = characterBody.getGlobalPosition();
        Vector3 movementLeft = playerBodyLocation.sub(newCharPos);
        movementLeft = new Vector3(movementLeft.x, 0.0, movementLeft.z);

        double locationOffset = movementLeft.length();
        if (locationOffset > 0.1) {
            double fadeValue = Math.max(0.0, Math.min(1.0, (locationOffset - 0.1) / 0.1));
            if (blackOut != null) blackOut.setFade(fadeValue);
            return true;
        } else {
            if (blackOut != null) blackOut.setFade(0.0);
            return false;
        }
    }

    private void copyPlayerRotationToCharacterBody() {
        if (characterBody == null || cameraNode == null) return;

        Transform3D cameraTf = cameraNode.getGlobalTransform();
        Basis cameraBasis = cameraTf.getBasis();
        Vector3 forward = new Vector3(-cameraBasis.zx, -cameraBasis.zy, -cameraBasis.zz);
        Vector3 bodyForward = new Vector3(forward.x, 0.0, forward.z);
        double len = bodyForward.length();
        if (len > 0.001) {
            bodyForward = bodyForward.div(len);
        }

        // Compute looking-at basis for bodyForward direction
        Vector3 fwd = bodyForward.normalized();
        Vector3 r = Vector3.UP.cross(fwd).normalized();
        Vector3 u = fwd.cross(r).normalized();
        Basis lookingBasis = new Basis(
                r.x, u.x, fwd.x,
                r.y, u.y, fwd.y,
                r.z, u.z, fwd.z);
        Vector3 charPos = characterBody.getGlobalPosition();
        characterBody.setGlobalTransform(new Transform3D(lookingBasis, charPos));
    }

    private void processMovementOnInput(boolean isColliding, double delta) {
        if (characterBody == null) return;

        Vector3 orgPlayerBody = characterBody.getGlobalPosition();

        if (!isColliding) {
            Vector2 movementInput = getMovementInput();

            // Handle rotation - rotate the origin around the player.
            Vector3 globalOrigin = getGlobalPosition();
            Vector3 charGlobalPos = characterBody.getGlobalPosition();
            Vector3 playerPosition = charGlobalPos.sub(globalOrigin);

            Transform3D t1 = new Transform3D(new Basis(), playerPosition.mul(-1));
            Transform3D t2 = new Transform3D(new Basis(), playerPosition);
            Transform3D rot = Transform3D.rotated(
                    new Vector3(0.0, 1.0, 0.0), -movementInput.x * delta * rotationSpeed);

            Transform3D currentGlobal = getGlobalTransform();
            Transform3D newGlobal = currentGlobal.multiply(t2).multiply(rot).multiply(t1);
            // orthonormalized
            // Orthonormalize: normalize the basis axes
            Basis nb = newGlobal.getBasis();
            Vector3 col0 = new Vector3(nb.xx, nb.yx, nb.zx).normalized();
            Vector3 col1 = col0.cross(new Vector3(nb.xz, nb.yz, nb.zz).normalized().normalized());
            Vector3 col2 = col0.cross(col1).normalized();
            Basis orthoBasis = new Basis(
                    col0.x, col1.x, col2.x,
                    col0.y, col1.y, col2.y,
                    col0.z, col1.z, col2.z);
            setGlobalTransform(new Transform3D(orthoBasis, newGlobal.getOrigin()));

            // Ensure player body is facing the correct way.
            copyPlayerRotationToCharacterBody();

            // Handle forward/backwards movement.
            Transform3D charGlobalTransform = characterBody.getGlobalTransform();
            Basis charBasis = charGlobalTransform.getBasis();
            Vector3 direction = new Vector3(
                    charBasis.zx * (-movementInput.y),
                    charBasis.zy * (-movementInput.y),
                    charBasis.zz * (-movementInput.y));
            direction = direction.mul(movementSpeed);

            Vector3 charVelocity = characterBody.getVelocity();
            if (direction.length() > 0.001) {
                charVelocity = new Vector3(
                        moveToward(charVelocity.x, direction.x, delta * movementAcceleration),
                        charVelocity.y,
                        moveToward(charVelocity.z, direction.z, delta * movementAcceleration));
            } else {
                charVelocity = new Vector3(
                        moveToward(charVelocity.x, 0, delta * movementAcceleration),
                        charVelocity.y,
                        moveToward(charVelocity.z, 0, delta * movementAcceleration));
            }
            characterBody.setVelocity(charVelocity);
        }

        // Always handle gravity.
        Vector3 charVelocity = characterBody.getVelocity();
        charVelocity = new Vector3(charVelocity.x, charVelocity.y - gravity * delta, charVelocity.z);
        characterBody.setVelocity(charVelocity);

        // Attempt to move our player.
        characterBody.moveAndSlide();

        // Apply actual movement to origin.
        Vector3 newCharPos = characterBody.getGlobalPosition();
        Vector3 deltaMovement = newCharPos.sub(orgPlayerBody);
        Vector3 originGlobalPos = getGlobalPosition();
        setGlobalPosition(originGlobalPos.add(deltaMovement));
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
