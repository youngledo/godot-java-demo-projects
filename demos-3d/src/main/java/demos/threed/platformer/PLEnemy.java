package demos.threed.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.math.Vector3;
import org.godot.node.AnimationPlayer;
import org.godot.node.AudioStreamPlayer;
import org.godot.node.Node3D;
import org.godot.node.RayCast3D;
import org.godot.node.RigidBody3D;
import org.godot.singleton.ProjectSettings;

@GodotClass(name = "PLEnemy", parent = "RigidBody3D")
public class PLEnemy extends RigidBody3D {

    private static final double ACCEL = 5.0;
    private static final double DEACCEL = 20.0;
    private static final double MAX_SPEED = 2.0;
    private static final double ROT_SPEED = 1.0;

    private boolean prevAdvance = false;
    private boolean dying = false;
    private double rotDir = 4.0;

    private Vector3 gravity = new Vector3(0, -9.8, 0);
    private AnimationPlayer animationPlayer;
    private RayCast3D rayFloor;
    private RayCast3D rayWall;
    private Node3D skeleton;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        ProjectSettings ps = ProjectSettings.singleton();
        if (ps != null) {
            Object gravVal = ps.getSetting("physics/3d/default_gravity");
            Object gravVecVal = ps.getSetting("physics/3d/default_gravity_vector");
            double gravMag = gravVal instanceof Number number ? number.doubleValue() : 9.8;
            Vector3 gravDir = gravVecVal instanceof Vector3 vector ? vector : new Vector3(0, -1, 0);
            gravity = gravDir.mul(gravMag);
        }

        animationPlayer = getNodeAs("Enemy/AnimationPlayer", AnimationPlayer.class);
        rayFloor = getNodeAs("Enemy/Skeleton/RayFloor", RayCast3D.class);
        rayWall = getNodeAs("Enemy/Skeleton/RayWall", RayCast3D.class);
        skeleton = getNodeAs("Enemy/Skeleton", Node3D.class);
    }

    @Override
    public void _physicsProcess(double delta) {
        if (dying) return;

        Vector3 linVelocity = getLinearVelocity();
        Vector3 up = gravity.mul(-1).normalized();

        boolean advance = false;
        if (rayFloor != null && rayWall != null) {
            advance = rayFloor.isColliding() && !rayWall.isColliding();
        }

        Vector3 dir = Vector3.FORWARD;
        if (skeleton != null) {
            Basis skelBasis = skeleton.getTransform().getBasis();
            dir = new Vector3(skelBasis.zx, skelBasis.zy, skelBasis.zz).normalized();
        }
        Vector3 deaccelDir = dir;

        if (advance) {
            if (dir.dot(linVelocity) < MAX_SPEED) {
                applyCentralForce(dir.mul(ACCEL));
            }
            deaccelDir = dir.cross(gravity).normalized();
        } else {
            if (prevAdvance) rotDir = 1;
            Basis rotBasis = Basis.fromAxisAngle(up, rotDir * ROT_SPEED * delta);
            dir = rotBasis.apply(dir);
            if (skeleton != null) {
                Vector3 fwd = dir.mul(-1);
                Vector3 col0 = fwd;
                Vector3 col1 = up;
                Vector3 col2 = col0.cross(col1).normalized();
                Transform3D lookXform = new Transform3D(col0, col1, col2, Vector3.ZERO);
                skeleton.setTransform(lookXform);
            }
        }

        double dspeed = deaccelDir.dot(linVelocity);
        dspeed -= DEACCEL * delta;
        if (dspeed < 0) dspeed = 0;

        linVelocity = linVelocity.sub(deaccelDir.mul(deaccelDir.dot(linVelocity))).add(deaccelDir.mul(dspeed));
        setLinearVelocity(linVelocity);

        prevAdvance = advance;
    }

    @GodotMethod
    public void destroy() {
        dying = true;
        setAxisLockAngularX(false);
        setAxisLockAngularY(false);
        setAxisLockAngularZ(false);
        setCollisionLayer(0L);

        if (animationPlayer != null) {
            animationPlayer.play("impact");
            animationPlayer.queue("extra/explode");
        }
        AudioStreamPlayer soundWalk = getNodeAs("SoundWalkLoop", AudioStreamPlayer.class);
        if (soundWalk != null) soundWalk.stop();
        AudioStreamPlayer soundHit = getNodeAs("SoundHit", AudioStreamPlayer.class);
        if (soundHit != null) soundHit.play();
    }
}
