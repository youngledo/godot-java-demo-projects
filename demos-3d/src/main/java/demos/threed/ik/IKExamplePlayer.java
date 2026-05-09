package demos.threed.ik;

import org.godot.annotation.GodotClass;
import org.godot.core.Callable;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.AnimationPlayer;
import org.godot.node.CharacterBody3D;
import org.godot.node.InputEventMouseMotion;
import org.godot.node.Node;
import org.godot.node.Node3D;
import org.godot.node.PackedScene;
import org.godot.node.PathFollow3D;
import org.godot.singleton.Input;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "IKExamplePlayer", parent = "CharacterBody3D")
public class IKExamplePlayer extends CharacterBody3D {

    private static final double NORM_GRAV = -38.8;
    private static final int MAX_SPEED = 22;
    private static final int JUMP_SPEED = 26;
    private static final double ACCEL = 8.5;
    private static final int MAX_SPRINT_SPEED = 34;
    private static final int SPRINT_ACCEL = 18;
    private static final int DEACCEL = 28;
    private static final double LEFT_MOUSE_FIRE_TIME = 0.15;
    private static final int BULLET_SPEED = 100;
    private static final double MOUSE_SENSITIVITY = 0.08;

    private Vector3 vel = new Vector3();
    private Vector3 dir = new Vector3();
    private boolean isSprinting = false;
    private boolean jumpButtonDown = false;
    private double leanValue = 0.5;
    private double leftMouseTimer = 0;

    private Node3D cameraHolder;
    private Node3D camera;
    private PathFollow3D pathFollowNode;
    private AnimationPlayer animPlayer;
    private Node3D pistolEnd;
    private Input input;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        input = Input.singleton();

        Node node = getNode("CameraHolder");
        if (node instanceof Node3D holder) {
            cameraHolder = holder;
            camera = holder.getNodeAs("LeanPath/PathFollow3D/IK_LookAt_Chest/Camera3D", Node3D.class);
            pathFollowNode = holder.getNodeAs("LeanPath/PathFollow3D", PathFollow3D.class);
            animPlayer = holder.getNodeAs("AnimationPlayer", AnimationPlayer.class);
            pistolEnd = holder.getNodeAs("Weapon/Pistol/PistolEnd", Node3D.class);
        }

        if (animPlayer != null) {
            animPlayer.connect("animation_finished", new Callable(this, "on_animation_finished"), 0);
        }

        input.setMouseMode(2);
    }

    @Override
    public void _physicsProcess(double delta) {
        processInput(delta);
        processMovement(delta);
    }

    @Override
    public boolean _input(Object event) {
        if (input == null) return false;

        if (event instanceof InputEventMouseMotion mouseMotion && input.getMouseMode() == 2) {
            Vector2 rel = mouseMotion.getScreenRelative();
            rotateY(Math.toRadians(rel.x * MOUSE_SENSITIVITY * -1));
            if (cameraHolder != null) {
                cameraHolder.rotateX(Math.toRadians(rel.y * MOUSE_SENSITIVITY));
                Vector3 rot = cameraHolder.getRotationDegrees();
                double x = Math.max(-40, Math.min(60, rot.x));
                cameraHolder.setRotationDegrees(new Vector3(x, rot.y, rot.z));
            }
        }
        return false;
    }

    private void processInput(double delta) {
        dir = new Vector3();

        if (camera == null || input == null) return;

        Transform3D camXform = camera.getGlobalTransform();
        Basis camBasis = camXform.getBasis();

        boolean upPressed = input.isKeyPressed(87) || input.isKeyPressed(4194320);
        boolean downPressed = input.isKeyPressed(83) || input.isKeyPressed(4194322);
        boolean leftPressed = input.isKeyPressed(65) || input.isKeyPressed(4194321);
        boolean rightPressed = input.isKeyPressed(68) || input.isKeyPressed(4194323);

        Vector3 fwd = new Vector3(camBasis.zx, camBasis.zy, camBasis.zz).mul(-1);
        Vector3 right = new Vector3(camBasis.xx, camBasis.xy, camBasis.xz);

        if (upPressed) dir = dir.add(fwd);
        if (downPressed) dir = dir.add(fwd.mul(-1));
        if (leftPressed) dir = dir.add(right.mul(-1));
        if (rightPressed) dir = dir.add(right);

        isSprinting = input.isKeyPressed(4194325);

        boolean spacePressed = input.isKeyPressed(32);
        if (spacePressed) {
            if (!jumpButtonDown) {
                jumpButtonDown = true;
                if (isOnFloor()) {
                    vel = new Vector3(vel.x, JUMP_SPEED, vel.z);
                }
            }
        } else {
            jumpButtonDown = false;
        }

        boolean qPressed = input.isKeyPressed(81);
        boolean ePressed = input.isKeyPressed(69);
        if (qPressed) {
            leanValue += 1.2 * delta;
        } else if (ePressed) {
            leanValue -= 1.2 * delta;
        } else {
            if (leanValue > 0.5) {
                leanValue -= 1.0 * delta;
                if (leanValue < 0.5) leanValue = 0.5;
            } else if (leanValue < 0.5) {
                leanValue += 1.0 * delta;
                if (leanValue > 0.5) leanValue = 0.5;
            }
        }
        leanValue = Math.max(0, Math.min(1, leanValue));

        if (pathFollowNode != null) {
            pathFollowNode.setHOffset(leanValue);
            double rotZ;
            if (leanValue < 0.5) {
                double lerpValue = leanValue * 2;
                rotZ = 20 * (1 - lerpValue);
            } else {
                double lerpValue = (leanValue - 0.5) * 2;
                rotZ = -20 * lerpValue;
            }
            Vector3 r = pathFollowNode.getRotationDegrees();
            pathFollowNode.setRotationDegrees(new Vector3(r.x, r.y, rotZ));
        }

        if (input.isMouseButtonPressed(1)) {
            if (leftMouseTimer <= 0) {
                leftMouseTimer = LEFT_MOUSE_FIRE_TIME;
                fireBullet();
            }
        }
        if (leftMouseTimer > 0) leftMouseTimer -= delta;
    }

    private void fireBullet() {
        if (pistolEnd == null) return;
        if (!(ResourceLoader.singleton().load("res://fps/simple_bullet.tscn") instanceof PackedScene bulletScene)) return;
        if (!(bulletScene.instantiate() instanceof IKSimpleBullet newBullet)) return;

        Node root = getTree().getRoot();
        if (root != null) root.addChild(newBullet);

        Transform3D bt = pistolEnd.getGlobalTransform();
        newBullet.setGlobalTransform(bt);
        Basis btBasis = bt.getBasis();
        Vector3 bulletDir = new Vector3(btBasis.zx, btBasis.zy, btBasis.zz);
        newBullet.setLinearVelocity(bulletDir.mul(BULLET_SPEED));
    }

    private void processMovement(double delta) {
        Vector3 d = new Vector3(dir.x, 0, dir.z);
        if (d.length() > 0) d = d.normalized();

        vel = new Vector3(vel.x, vel.y + delta * NORM_GRAV, vel.z);

        Vector3 hvel = new Vector3(vel.x, 0, vel.z);
        Vector3 target = d.mul(isSprinting ? MAX_SPRINT_SPEED : MAX_SPEED);

        double accel = d.dot(hvel) > 0 ? isSprinting ? SPRINT_ACCEL : ACCEL : DEACCEL;

        hvel = hvel.lerp(target, accel * delta);
        vel = new Vector3(hvel.x, vel.y, hvel.z);

        setVelocity(vel);
        moveAndSlide();
        vel = getVelocity();
        if (vel == null) vel = new Vector3();
    }

    public void onAnimationFinished(String animName) {
    }
}
