package demos.xr.openxr_hand_tracking_demo.pickup;

import java.util.ArrayList;
import java.util.List;
import org.godot.annotation.GodotClass;
import org.godot.math.Transform3D;
import org.godot.node.MeshInstance3D;
import org.godot.node.Node;
import org.godot.node.RigidBody3D;
import org.godot.node.Tween;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "PickupAbleBody3D", parent = "RigidBody3D")
public class PickupAbleBody extends RigidBody3D {

    private Object highlightMaterial;
    private Node pickedUpBy;
    private List<Node> closestAreas = new ArrayList<>();
    private Node originalParent;
    private Tween tween;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        highlightMaterial = ResourceLoader.singleton().load("res://shaders/highlight_material.tres");
    }

    public void addIsClosest(Node area) {
        if (!closestAreas.contains(area)) {
            closestAreas.add(area);
        }
        updateHighlight();
    }

    public void removeIsClosest(Node area) {
        closestAreas.remove(area);
        updateHighlight();
    }

    public boolean isPickedUp() {
        return pickedUpBy != null;
    }

    public void pickUp(Node pickUpByNode) {
        if (pickedUpBy != null) {
            if (pickedUpBy == pickUpByNode) return;
            letGo();
        }

        originalParent = getParent();
        Transform3D currentTransform = getGlobalTransform();

        originalParent.removeChild(this);

        pickedUpBy = pickUpByNode;
        pickedUpBy.addChild(this);
        setGlobalTransform(currentTransform);
        setFreeze(true);

        if (tween != null) {
            tween.kill();
        }
        tween = createTween();

        Transform3D snapTo = new Transform3D();
        tween.tweenProperty(this, "transform", snapTo, 0.1);
    }

    public void letGo() {
        if (pickedUpBy == null) return;

        if (tween != null) {
            tween.kill();
            tween = null;
        }

        Transform3D currentTransform = getGlobalTransform();

        pickedUpBy.removeChild(this);
        pickedUpBy = null;

        originalParent.addChild(this);
        setGlobalTransform(currentTransform);
        setFreeze(false);
    }

    private void updateHighlight() {
        Object materialOverlay = pickedUpBy == null && !closestAreas.isEmpty() ? highlightMaterial : null;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            Node child = getChild(i);
            if (child instanceof MeshInstance3D mesh) {
                mesh.setMaterialOverlay(materialOverlay);
            }
        }
    }
}
