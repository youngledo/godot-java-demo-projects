package demos.threed.ik;

import org.godot.annotation.GodotClass;
import org.godot.node.SkeletonIK3D;

@GodotClass(name = "IKSkeletonRunner", parent = "SkeletonIK3D")
public class IKSkeletonRunner extends SkeletonIK3D {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        call("start", false);
    }
}
