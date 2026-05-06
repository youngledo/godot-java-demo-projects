package demos.xr.openxr_render_models;

import org.godot.annotation.GodotClass;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "CollisionHands3D", parent = "AnimatableBody3D")
public class CollisionHands extends org.godot.Godot {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Make sure these are set correctly.
        setProperty("top_level", true);
        setProperty("sync_to_physics", false);
        setProperty("process_physics_priority", -90);
    }

    @Override
    public void _physicsProcess(double delta) {
        // Follow our parent node around.
        org.godot.node.Node3D parent = (org.godot.node.Node3D) call("get_parent");
        if (parent == null) return;

        org.godot.math.Transform3D destTransform = (org.godot.math.Transform3D) parent.getGlobalTransform();

        // We just apply rotation for this example.
        setProperty("global_basis", destTransform.getBasis());

        // Attempt to move to where our tracked hand is.
        org.godot.math.Vector3 globalPos = (org.godot.math.Vector3) call("get_global_position");
        org.godot.math.Vector3 deltaPos = destTransform.getOrigin().sub(globalPos);
        call("move_and_collide", deltaPos);
    }
}
