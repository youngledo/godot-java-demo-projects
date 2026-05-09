package demos.xr.openxr_render_models;

import org.godot.annotation.GodotClass;
import org.godot.math.Transform3D;
import org.godot.math.Vector3;
import org.godot.node.AnimatableBody3D;
import org.godot.node.Node;
import org.godot.node.Node3D;

@GodotClass(name = "CollisionHands3D", parent = "AnimatableBody3D")
public class CollisionHands extends AnimatableBody3D {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        setTopLevel(true);
        setSyncToPhysics(false);
        setProcessPhysicsPriority(-90);
    }

    @Override
    public void _physicsProcess(double delta) {
        Node parent = getParent();
        if (!(parent instanceof Node3D parent3D)) return;

        Transform3D destTransform = parent3D.getGlobalTransform();
        setGlobalBasis(destTransform.getBasis());

        Vector3 deltaPos = destTransform.getOrigin().sub(getGlobalPosition());
        moveAndCollide(deltaPos);
    }
}
