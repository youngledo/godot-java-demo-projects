package demos.xr.openxr_hand_tracking_demo.pickup;

import org.godot.annotation.GodotClass;
import org.godot.node.RigidBody3D;

import java.util.ArrayList;
import java.util.List;

@GodotClass(name = "PickupAbleBody3D", parent = "RigidBody3D")
public class PickupAbleBody extends RigidBody3D {

    private org.godot.Godot highlightMaterial;
    private org.godot.Godot pickedUpBy;
    private List<org.godot.Godot> closestAreas = new ArrayList<>();
    private org.godot.Godot originalParent;
    private org.godot.Godot tween;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Load highlight material.
        highlightMaterial = (org.godot.Godot) call("load", "res://shaders/highlight_material.tres");
    }

    public void addIsClosest(org.godot.Godot area) {
        if (!closestAreas.contains(area)) {
            closestAreas.add(area);
        }
        updateHighlight();
    }

    public void removeIsClosest(org.godot.Godot area) {
        closestAreas.remove(area);
        updateHighlight();
    }

    public boolean isPickedUp() {
        return pickedUpBy != null;
    }

    public void pickUp(org.godot.Godot pickUpByNode) {
        if (pickedUpBy != null) {
            if (pickedUpBy == pickUpByNode) return;
            letGo();
        }

        // Remember state.
        originalParent = (org.godot.Godot) call("get_parent");
        org.godot.math.Transform3D currentTransform = (org.godot.math.Transform3D) call("get_global_transform");

        // Remove from old parent.
        originalParent.call("remove_child", this);

        // Process pickup.
        pickedUpBy = pickUpByNode;
        pickedUpBy.call("add_child", this);
        call("set_global_transform", currentTransform);
        setProperty("freeze", true);

        // Kill existing tween and create new.
        if (tween != null) {
            tween.call("kill");
        }
        tween = (org.godot.Godot) call("create_tween");

        // Snap to transform (identity for now).
        org.godot.math.Transform3D snapTo = new org.godot.math.Transform3D();
        tween.call("tween_property", this, "transform", snapTo, 0.1);
    }

    public void letGo() {
        if (pickedUpBy == null) return;

        // Cancel ongoing tween.
        if (tween != null) {
            tween.call("kill");
            tween = null;
        }

        org.godot.math.Transform3D currentTransform = (org.godot.math.Transform3D) call("get_global_transform");

        pickedUpBy.call("remove_child", this);
        pickedUpBy = null;

        originalParent.call("add_child", this);
        call("set_global_transform", currentTransform);
        setProperty("freeze", false);
    }

    private void updateHighlight() {
        if (pickedUpBy == null && !closestAreas.isEmpty()) {
            // Add highlight.
            int childCount = (int) call("get_child_count");
            for (int i = 0; i < childCount; i++) {
                org.godot.Godot child = (org.godot.Godot) call("get_child", i);
                if (child != null && "MeshInstance3D".equals(child.call("get_class"))) {
                    child.setProperty("material_overlay", highlightMaterial);
                }
            }
        } else {
            // Remove highlight.
            int childCount = (int) call("get_child_count");
            for (int i = 0; i < childCount; i++) {
                org.godot.Godot child = (org.godot.Godot) call("get_child", i);
                if (child != null && "MeshInstance3D".equals(child.call("get_class"))) {
                    child.setProperty("material_overlay", null);
                }
            }
        }
    }
}
