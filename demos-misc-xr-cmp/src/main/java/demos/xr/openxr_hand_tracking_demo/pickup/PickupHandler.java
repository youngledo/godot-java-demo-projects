package demos.xr.openxr_hand_tracking_demo.pickup;

import org.godot.annotation.GodotClass;
import org.godot.node.Area3D;
import org.godot.node.Node;

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
        org.godot.node.CollisionShape3D collisionShape = (org.godot.node.CollisionShape3D) getNode("CollisionShape3D");
        if (collisionShape != null) {
            org.godot.Godot shape = (org.godot.Godot) collisionShape.getProperty("shape");
            if (shape != null) {
                shape.setProperty("radius", detectRange);
            }
        }
    }

    private void updateClosestBody() {
        // Don't check if we've picked something up.
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
                if (bodyObj instanceof PickupAbleBody) {
                    PickupAbleBody body = (PickupAbleBody) bodyObj;
                    if (!body.isPickedUp()) {
                        org.godot.math.Vector3 bodyPos = (org.godot.math.Vector3) body.getGlobalPosition();
                        double distSq = bodyPos.distanceSquaredTo(globalPos);
                        if (distSq < closestDistance) {
                            newClosestBody = body;
                            closestDistance = distSq;
                        }
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

    private org.godot.Godot getParentController() {
        org.godot.Godot parent = (org.godot.Godot) getParent();
        while (parent != null) {
            String className = (String) parent.call("get_class");
            if ("XRController3D".equals(className)) {
                return parent;
            }
            parent = (org.godot.Godot) parent.call("get_parent");
        }
        return null;
    }

    @Override
    public void _physicsProcess(double delta) {
        updateClosestBody();

        // Check if our pickup action is true.
        boolean pickupPressed = false;
        org.godot.Godot controller = getParentController();
        if (controller != null) {
            double pickupValue = (double) controller.call("get_float", pickupAction);
            double threshold = wasPickupPressed ? 0.4 : 0.6;
            pickupPressed = pickupValue > threshold;
        }

        // Do we need to let go?
        if (pickedUpBody != null && !pickupPressed) {
            pickedUpBody.letGo();
            pickedUpBody = null;
        }

        // Do we need to pick something up?
        if (pickedUpBody == null && !wasPickupPressed && pickupPressed && closestBody != null) {
            pickedUpBody = closestBody;
            pickedUpBody.pickUp(this);
        }

        wasPickupPressed = pickupPressed;
    }
}
