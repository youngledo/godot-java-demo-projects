package demos.xr.openxr_hand_tracking_demo.pickup;

import org.godot.annotation.GodotClass;
import org.godot.node.Area3D;
import org.godot.node.CollisionShape3D;
import org.godot.node.Node;
import org.godot.node.Shape3D;
import org.godot.node.SphereShape3D;
import org.godot.node.XRController3D;

@GodotClass(name = "PickupHandler3D", parent = "Area3D")
public class PickupHandler extends Area3D {

    private double detectRange = 0.3;
    private String pickupAction = "pickup";

    private PickupAbleBody closestBody;
    private PickupAbleBody pickedUpBody;
    private boolean wasPickupPressed = false;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        updateDetectRange();
    }

    private void updateDetectRange() {
        CollisionShape3D collisionShape = getNodeAs("CollisionShape3D", CollisionShape3D.class);
        if (collisionShape != null) {
            Shape3D shape = collisionShape.getShape();
            if (shape instanceof SphereShape3D sphereShape) {
                sphereShape.setRadius(detectRange);
            }
        }
    }

    private void updateClosestBody() {
        if (pickedUpBody != null) {
            if (closestBody != null) {
                closestBody.removeIsClosest(this);
                closestBody = null;
            }
            return;
        }

        PickupAbleBody newClosestBody = null;
        double closestDistance = 1000000.0;

        Object[] bodies = getOverlappingBodies();
        if (bodies != null) {
            org.godot.math.Vector3 globalPos = getGlobalPosition();
            for (Object bodyObj : bodies) {
                if (bodyObj instanceof PickupAbleBody body && !body.isPickedUp()) {
                    org.godot.math.Vector3 bodyPos = body.getGlobalPosition();
                    double distSq = bodyPos.distanceSquaredTo(globalPos);
                    if (distSq < closestDistance) {
                        newClosestBody = body;
                        closestDistance = distSq;
                    }
                }
            }
        }

        if (closestBody == newClosestBody) return;

        if (closestBody != null) {
            closestBody.removeIsClosest(this);
        }

        closestBody = newClosestBody;
        if (closestBody != null) {
            closestBody.addIsClosest(this);
        }
    }

    private XRController3D getParentController() {
        Node parent = getParent();
        while (parent != null) {
            if (parent instanceof XRController3D controller) {
                return controller;
            }
            parent = parent.getParent();
        }
        return null;
    }

    @Override
    public void _physicsProcess(double delta) {
        updateClosestBody();

        boolean pickupPressed = false;
        XRController3D controller = getParentController();
        if (controller != null) {
            double pickupValue = controller.getFloat(pickupAction);
            double threshold = wasPickupPressed ? 0.4 : 0.6;
            pickupPressed = pickupValue > threshold;
        }

        if (pickedUpBody != null && !pickupPressed) {
            pickedUpBody.letGo();
            pickedUpBody = null;
        }

        if (pickedUpBody == null && !wasPickupPressed && pickupPressed && closestBody != null) {
            pickedUpBody = closestBody;
            pickedUpBody.pickUp(this);
        }

        wasPickupPressed = pickupPressed;
    }
}
