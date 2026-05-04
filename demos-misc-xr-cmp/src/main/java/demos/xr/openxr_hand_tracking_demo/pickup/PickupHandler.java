package demos.xr.openxr_hand_tracking_demo.pickup;

import org.godot.annotation.GodotClass;
import org.godot.node.Area3D;

@GodotClass(name = "PickupHandler3D", parent = "Area3D")
public class PickupHandler extends Area3D {

    private double detectRange = 0.3;
    private String pickupAction = "pickup";

    private org.godot.Godot closestBody;
    private org.godot.Godot pickedUpBody;
    private boolean wasPickupPressed = false;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        updateDetectRange();
    }

    private void updateDetectRange() {
        org.godot.Godot collisionShape = (org.godot.Godot) call("get_node", "CollisionShape3D");
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
                closestBody.call("remove_is_closest", this);
                closestBody = null;
            }
            return;
        }

        org.godot.Godot newClosestBody = null;
        double closestDistance = 1000000.0;

        Object[] bodies = (Object[]) call("get_overlapping_bodies");
        if (bodies != null) {
            org.godot.math.Vector3 globalPos = (org.godot.math.Vector3) call("get_global_position");
            for (Object bodyObj : bodies) {
                if (bodyObj instanceof org.godot.Godot) {
                    org.godot.Godot body = (org.godot.Godot) bodyObj;
                    String className = (String) body.call("get_class");
                    if ("PickupAbleBody3D".equals(className) || body instanceof PickupAbleBody) {
                        boolean isPickedUp = (boolean) body.call("is_picked_up");
                        if (!isPickedUp) {
                            org.godot.math.Vector3 bodyPos = (org.godot.math.Vector3) body.call("get_global_position");
                            double distSq = bodyPos.distanceSquaredTo(globalPos);
                            if (distSq < closestDistance) {
                                newClosestBody = body;
                                closestDistance = distSq;
                            }
                        }
                    }
                }
            }
        }

        if (closestBody == newClosestBody) return;

        if (closestBody != null) {
            closestBody.call("remove_is_closest", this);
        }

        closestBody = newClosestBody;
        if (closestBody != null) {
            closestBody.call("add_is_closest", this);
        }
    }

    private org.godot.Godot getParentController() {
        org.godot.Godot parent = (org.godot.Godot) call("get_parent");
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
            pickedUpBody.call("let_go");
            pickedUpBody = null;
        }

        // Do we need to pick something up?
        if (pickedUpBody == null && !wasPickupPressed && pickupPressed && closestBody != null) {
            pickedUpBody = closestBody;
            pickedUpBody.call("pick_up", this);
        }

        wasPickupPressed = pickupPressed;
    }
}
