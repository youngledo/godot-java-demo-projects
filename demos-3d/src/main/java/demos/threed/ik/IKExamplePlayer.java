package demos.threed.ik;

import org.godot.annotation.GodotClass;
import org.godot.node.CharacterBody3D;
import org.godot.math.Vector3;
import org.godot.math.Basis;
import org.godot.math.Transform3D;
import org.godot.node.Node;
import org.godot.singleton.Input;

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
    private boolean rightMouseDown = false;
    private double leftMouseTimer = 0;
    private boolean animDone = true;
    private String currentAnim = "Starter";

    private org.godot.node.Node cameraHolder;
    private org.godot.node.Node3D camera;
    private org.godot.Godot pathFollowNode;
    private org.godot.node.AnimationPlayer animPlayer;
    private org.godot.node.Node3D pistolEnd;
    private org.godot.singleton.Input input;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        input = org.godot.singleton.Input.singleton();

        cameraHolder = getNode("CameraHolder");
        if (cameraHolder != null) {
            camera = (org.godot.node.Node3D) cameraHolder.getNode("LeanPath/PathFollow3D/IK_LookAt_Chest/Camera3D");
            pathFollowNode = (org.godot.node.Node3D) cameraHolder.getNode("LeanPath/PathFollow3D");
            animPlayer = (org.godot.node.AnimationPlayer) cameraHolder.getNode("AnimationPlayer");
            pistolEnd = (org.godot.node.Node3D) cameraHolder.getNode("Weapon/Pistol/PistolEnd");
        }

        if (animPlayer != null) {
            animPlayer.connect("animation_finished", new org.godot.core.Callable(this, "on_animation_finished"), 0);
        }

        if (input != null) {
            input.setMouseMode(2); // MOUSE_MODE_CAPTURED
        }
    }

    @Override
    public void _physicsProcess(double delta) {
        processInput(delta);
        processMovement(delta);
    }

    @Override
    public boolean _input(Object event) {
        if (input == null) return false;

        Object isMotion = ((org.godot.Godot) event).call("is_class", "InputEventMouseMotion");
        if (isMotion instanceof Boolean && (Boolean) isMotion) {
            Object mouseMode = input.getMouseMode();
            if (mouseMode instanceof Number && ((Number) mouseMode).intValue() == 2) {
                Object relObj = ((org.godot.Godot) event).getProperty("screen_relative");
                if (relObj instanceof org.godot.math.Vector2) {
                    org.godot.math.Vector2 rel = (org.godot.math.Vector2) relObj;
                    rotateY(Math.toRadians(rel.x * MOUSE_SENSITIVITY * -1));
                    if (cameraHolder != null) {
                        cameraHolder.call("rotate_x", Math.toRadians(rel.y * MOUSE_SENSITIVITY));
                        Object rotObj = cameraHolder.getProperty("rotation_degrees");
                        if (rotObj instanceof Vector3) {
                            Vector3 rot = (Vector3) rotObj;
                            double x = Math.max(-40, Math.min(60, rot.x));
                            cameraHolder.setProperty("rotation_degrees", new Vector3(x, rot.y, rot.z));
                        }
                    }
                }
            }
        }
        return false;
    }

    private void processInput(double delta) {
        dir = new Vector3();

        if (camera == null) return;
        Object camXformObj = camera.getGlobalTransform();
        if (!(camXformObj instanceof Transform3D)) return;
        Transform3D camXform = (Transform3D) camXformObj;
        Basis camBasis = camXform.getBasis();

        if (input == null) return;

        // Walking - using key checks
        boolean upPressed = (boolean) input.isKeyPressed(87) || (boolean) input.isKeyPressed(4194320);
        boolean downPressed = (boolean) input.isKeyPressed(83) || (boolean) input.isKeyPressed(4194322);
        boolean leftPressed = (boolean) input.isKeyPressed(65) || (boolean) input.isKeyPressed(4194321);
        boolean rightPressed = (boolean) input.isKeyPressed(68) || (boolean) input.isKeyPressed(4194323);

        Vector3 fwd = new Vector3(camBasis.zx, camBasis.zy, camBasis.zz).mul(-1);
        Vector3 right = new Vector3(camBasis.xx, camBasis.xy, camBasis.xz);

        if (upPressed) dir = dir.add(fwd);
        if (downPressed) dir = dir.add(fwd.mul(-1));
        if (leftPressed) dir = dir.add(right.mul(-1));
        if (rightPressed) dir = dir.add(right);

        // Sprinting
        isSprinting = (boolean) input.isKeyPressed(4194325); // KEY_SHIFT

        // Jumping
        boolean spacePressed = (boolean) input.isKeyPressed(32); // KEY_SPACE
        if (spacePressed) {
            if (!jumpButtonDown) {
                jumpButtonDown = true;
                if (((boolean) isOnFloor())) {
                    vel = new Vector3(vel.x, JUMP_SPEED, vel.z);
                }
            }
        } else {
            jumpButtonDown = false;
        }

        // Leaning
        boolean qPressed = (boolean) input.isKeyPressed(81); // KEY_Q
        boolean ePressed = (boolean) input.isKeyPressed(69); // KEY_E
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
            pathFollowNode.setProperty("h_offset", leanValue);
            double rotZ;
            if (leanValue < 0.5) {
                double lerpValue = leanValue * 2;
                rotZ = 20 * (1 - lerpValue);
            } else {
                double lerpValue = (leanValue - 0.5) * 2;
                rotZ = -20 * lerpValue;
            }
            Object pfRot = pathFollowNode.getProperty("rotation_degrees");
            if (pfRot instanceof Vector3) {
                Vector3 r = (Vector3) pfRot;
                pathFollowNode.setProperty("rotation_degrees", new Vector3(r.x, r.y, rotZ));
            }
        }

        // Shooting
        boolean mouse1 = (boolean) input.isMouseButtonPressed(1);
        if (mouse1) {
            if (leftMouseTimer <= 0) {
                leftMouseTimer = LEFT_MOUSE_FIRE_TIME;
                fireBullet();
            }
        }
        if (leftMouseTimer > 0) leftMouseTimer -= delta;
    }

    private void fireBullet() {
        if (pistolEnd == null) return;
        org.godot.node.PackedScene bulletSceneObj = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://fps/simple_bullet.tscn");
        if (bulletSceneObj == null) return;

        org.godot.Godot newBullet = bulletSceneObj.instantiate();
        if (newBullet == null) return;

        org.godot.node.Node root = (org.godot.node.Node) (getTree().call("get_root"));
        if (root != null) root.addChild((org.godot.node.Node) newBullet);

        Object btObj = pistolEnd.getGlobalTransform();
        if (btObj instanceof Transform3D) {
            Transform3D bt = (Transform3D) btObj;
            newBullet.call("set_global_transform", bt);
            Basis btBasis = bt.getBasis();
            Vector3 bulletDir = new Vector3(btBasis.zx, btBasis.zy, btBasis.zz);
            newBullet.setProperty("linear_velocity", bulletDir.mul(BULLET_SPEED));
        }
    }

    private void processMovement(double delta) {
        Vector3 d = new Vector3(dir.x, 0, dir.z);
        if (d.length() > 0) d = d.normalized();

        vel = new Vector3(vel.x, vel.y + delta * NORM_GRAV, vel.z);

        Vector3 hvel = new Vector3(vel.x, 0, vel.z);
        Vector3 target = d.mul(isSprinting ? MAX_SPRINT_SPEED : MAX_SPEED);

        double accel;
        if (d.dot(hvel) > 0) {
            accel = isSprinting ? SPRINT_ACCEL : ACCEL;
        } else {
            accel = DEACCEL;
        }

        hvel = hvel.lerp(target, accel * delta);
        vel = new Vector3(hvel.x, vel.y, hvel.z);

        setProperty("velocity", vel);
        moveAndSlide();
        vel = (Vector3) getProperty("velocity");
        if (vel == null) vel = new Vector3();
    }

    public void onAnimationFinished(String animName) {
        animDone = true;
    }
}
