package demos.threed.ik;

import org.godot.annotation.GodotClass;
import org.godot.node.RigidBody3D;

@GodotClass(name = "IKSimpleBullet", parent = "RigidBody3D")
public class IKSimpleBullet extends RigidBody3D {

    private static final double DESPAWN_TIME = 5.0;
    private double timer = 0;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    @Override
    public void _physicsProcess(double delta) {
        timer += delta;
        if (timer > DESPAWN_TIME) {
            queueFree();
            timer = 0;
        }
    }
}
