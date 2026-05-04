package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.math.Basis;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.math.Transform3D;
import org.godot.node.CharacterBody3D;

/**
 * FPS player controller with mouse look, WASD, crouch, sprint, and block interaction.
 * Port of player/player.gd.
 */
@GodotClass(name = "VXPlayer", parent = "CharacterBody3D")
public class VXPlayer extends CharacterBody3D {

    private static final double EYE_HEIGHT_STAND = 1.6;
    private static final double EYE_HEIGHT_CROUCH = 1.4;

    private static final double MOVEMENT_SPEED_GROUND = 70.0;
    private static final double MOVEMENT_SPEED_AIR = 13.0;
    private static final double MOVEMENT_SPEED_CROUCH_MODIFIER = 0.5;
    private static final double MOVEMENT_SPEED_SPRINT_MODIFIER = 1.375;
    private static final double MOVEMENT_FRICTION_GROUND = 12.5;
    private static final double MOVEMENT_FRICTION_AIR = 2.25;
    private static final double MOVEMENT_JUMP_VELOCITY = 9.0;

    private Vector2 mouseMotion = new Vector2(0, 0);
    private int selectedBlock = 6;

    private org.godot.Godot head;
    private org.godot.Godot camera;
    private org.godot.Godot raycast;
    private org.godot.Godot cameraAttributes;
    private org.godot.Godot selectedBlockTexture;
    private org.godot.Godot crosshair;
    private org.godot.Godot aimPreview;
    private double neutralFov = 70.0;

    private org.godot.Godot voxelWorld;
    private org.godot.Godot settings;
    private org.godot.singleton.Input input;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        input = org.godot.singleton.Input.singleton();

        head = (org.godot.Godot) call("get_node", "Head");
        if (head != null) {
            camera = (org.godot.Godot) head.call("get_node", "Camera3D");
            raycast = (org.godot.Godot) head.call("get_node", "RayCast3D");
        }
        if (camera != null) {
            cameraAttributes = (org.godot.Godot) camera.getProperty("attributes");
        }
        selectedBlockTexture = (org.godot.Godot) call("get_node", "SelectedBlock");
        crosshair = (org.godot.Godot) call("get_node", "../PauseMenu/Crosshair");
        aimPreview = (org.godot.Godot) call("get_node", "AimPreview");

        if (camera != null) {
            Object fovObj = camera.getProperty("fov");
            if (fovObj instanceof Double) neutralFov = (Double) fovObj;
            else if (fovObj instanceof Long) neutralFov = ((Long) fovObj).doubleValue();
        }

        voxelWorld = (org.godot.Godot) call("get_node", "../VoxelWorld");
        settings = (org.godot.Godot) call("get_node", "/root/Settings");

        input.call("set_mouse_mode", 2); // MOUSE_MODE_CAPTURED = 2
    }

    @Override
    public void _process(double delta) {
        if (input == null) return;

        // Mouse movement - rotate body and head.
        double mx = mouseMotion.x;
        double my = Math.max(-1560, Math.min(1560, mouseMotion.y));

        // Rotate body around Y axis.
        Basis bodyBasis = Basis.fromAxisAngle(Vector3.UP, -mx * 0.001);
        setTransform(new Transform3D(bodyBasis, getPosition()));

        // Rotate head around X axis.
        if (head != null) {
            Basis headBasis = Basis.fromAxisAngle(Vector3.RIGHT, -my * 0.001);
            Object headTransform = head.call("get_transform");
            if (headTransform instanceof Transform3D) {
                Transform3D ht = (Transform3D) headTransform;
                head.call("set_transform", new Transform3D(headBasis, ht.getOrigin()));
            }
        }

        // Block selection via raycast.
        if (raycast != null && (boolean) raycast.call("is_colliding")) {
            Vector3 rayPosition = (Vector3) raycast.call("get_collision_point");
            Vector3 rayNormal = (Vector3) raycast.call("get_collision_normal");

            if ((boolean) input.call("is_action_just_pressed", "pick_block")) {
                // Block picking - identify block the ray is pointing at.
                Vector3 blockGlobal = new Vector3(
                        Math.floor(rayPosition.getX() - rayNormal.getX() / 2),
                        Math.floor(rayPosition.getY() - rayNormal.getY() / 2),
                        Math.floor(rayPosition.getZ() - rayNormal.getZ() / 2));
                int bpx = (int) Math.floor(blockGlobal.getX());
                int bpy = (int) Math.floor(blockGlobal.getY());
                int bpz = (int) Math.floor(blockGlobal.getZ());
                int sx = ((bpx % 16) + 16) % 16;
                int sy = ((bpy % 16) + 16) % 16;
                int sz = ((bpz % 16) + 16) % 16;
                int cx = (int) Math.floor((double) (bpx - sx) / 16);
                int cy = (int) Math.floor((double) (bpy - sy) / 16);
                int cz = (int) Math.floor((double) (bpz - sz) / 16);
                if (voxelWorld != null) {
                    selectedBlock = (int) voxelWorld.call("get_block_in_chunk", cx, cy, cz, sx, sy, sz);
                }
            } else {
                if ((boolean) input.call("is_action_just_pressed", "prev_block")) {
                    selectedBlock -= 1;
                }
                if ((boolean) input.call("is_action_just_pressed", "next_block")) {
                    selectedBlock += 1;
                }
                // Wrap within 1..29 range.
                selectedBlock = ((selectedBlock - 1) % 29 + 29) % 29 + 1;
            }
        }

        // Update selected block texture.
        if (selectedBlockTexture != null) {
            double[][] uvs = VXChunk.calculateBlockUVs(selectedBlock);
            org.godot.Godot tex = (org.godot.Godot) selectedBlockTexture.getProperty("texture");
            if (tex != null) {
                double u0 = uvs[0][0] * 512;
                double v0 = uvs[0][1] * 512;
                tex.setProperty("region", new org.godot.math.Rect2(u0, v0, 64, 64));
            }
        }

        // Block breaking/placing.
        boolean crosshairVisible = crosshair != null && (boolean) crosshair.call("is_visible");
        boolean rayColliding = raycast != null && (boolean) raycast.call("is_colliding");

        if (crosshairVisible && rayColliding && raycast != null) {
            if (aimPreview != null) aimPreview.call("set_visible", true);

            Vector3 rayPosition = (Vector3) raycast.call("get_collision_point");
            Vector3 rayNormal = (Vector3) raycast.call("get_collision_normal");

            int rayBlockX = (int) Math.floor(rayPosition.getX() - rayNormal.getX() / 2);
            int rayBlockY = (int) Math.floor(rayPosition.getY() - rayNormal.getY() / 2);
            int rayBlockZ = (int) Math.floor(rayPosition.getZ() - rayNormal.getZ() / 2);

            if (aimPreview != null) {
                aimPreview.call("set_global_position",
                        new Vector3(rayBlockX + 0.5, rayBlockY + 0.5, rayBlockZ + 0.5));
            }

            boolean breaking = (boolean) input.call("is_action_just_pressed", "break");
            boolean placing = (boolean) input.call("is_action_just_pressed", "place");

            // Either both buttons were pressed or neither is, so stop.
            if (breaking != placing) {
                if (breaking && voxelWorld != null) {
                    voxelWorld.call("set_block_global_position", rayBlockX, rayBlockY, rayBlockZ, 0);
                } else if (placing && voxelWorld != null) {
                    int placeX = (int) Math.floor(rayPosition.getX() + rayNormal.getX() / 2);
                    int placeY = (int) Math.floor(rayPosition.getY() + rayNormal.getY() / 2);
                    int placeZ = (int) Math.floor(rayPosition.getZ() + rayNormal.getZ() / 2);
                    voxelWorld.call("set_block_global_position", placeX, placeY, placeZ, selectedBlock);
                }
            }
        } else {
            if (aimPreview != null) aimPreview.call("set_visible", false);
        }
    }

    @Override
    public void _physicsProcess(double delta) {
        if (input == null) return;

        // Camera DOF blur based on fog settings.
        if (cameraAttributes != null && settings != null) {
            Object fogEnabled = settings.getProperty("fog_enabled");
            Object fogDist = settings.getProperty("fog_distance");
            boolean fogOn = fogEnabled instanceof Boolean && (Boolean) fogEnabled;
            double fogD = fogDist instanceof Double ? (Double) fogDist : 32.0;
            cameraAttributes.setProperty("dof_blur_far_enabled", fogOn);
            cameraAttributes.setProperty("dof_blur_far_distance", fogD * 1.5);
            cameraAttributes.setProperty("dof_blur_far_transition", fogD * 0.125);
        }

        // Crouching.
        boolean crouching = (boolean) input.call("is_action_pressed", "crouch");
        boolean sprinting = (boolean) input.call("is_action_pressed", "move_sprint");

        if (head != null) {
            Object headOriginObj = head.getProperty("position");
            Vector3 headOrigin = headOriginObj instanceof Vector3 ? (Vector3) headOriginObj : new Vector3(0, EYE_HEIGHT_STAND, 0);
            double targetY = crouching ? EYE_HEIGHT_CROUCH : EYE_HEIGHT_STAND;
            double newY = headOrigin.getY() + (targetY - headOrigin.getY()) * (1.0 - Math.exp(-delta * 16.0));
            head.setProperty("position", new Vector3(headOrigin.getX(), newY, headOrigin.getZ()));
        }

        // Keyboard movement via get_vector.
        Object moveVecObj = input.call("get_vector", "move_left", "move_right", "move_forward", "move_back");
        double moveX = 0, moveZ = 0;
        if (moveVecObj instanceof Vector2) {
            Vector2 mv = (Vector2) moveVecObj;
            moveX = mv.x;
            moveZ = mv.y;
        }

        // Transform movement by body basis.
        Transform3D transform = getTransform();
        Basis basis = transform.getBasis();
        Vector3 col0 = new Vector3(basis.xx, basis.xy, basis.xz);
        Vector3 col2 = new Vector3(basis.zx, basis.zy, basis.zz);
        Vector3 movement = col0.mul(moveX).add(col2.mul(moveZ));

        boolean onFloor = is_on_floor();
        if (onFloor) {
            movement = movement.mul(MOVEMENT_SPEED_GROUND);
        } else {
            movement = movement.mul(MOVEMENT_SPEED_AIR);
        }

        if (crouching) {
            movement = movement.mul(MOVEMENT_SPEED_CROUCH_MODIFIER);
            sprinting = false;
        }

        double targetFov = neutralFov;
        if (sprinting) {
            movement = movement.mul(MOVEMENT_SPEED_SPRINT_MODIFIER);
            targetFov = neutralFov * 1.25;
        }

        // FOV interpolation.
        if (camera != null) {
            Object fovObj = camera.getProperty("fov");
            double currentFov = fovObj instanceof Double ? (Double) fovObj : neutralFov;
            double newFov = currentFov + (targetFov - currentFov) * (1.0 - Math.exp(-delta * 10.0));
            camera.setProperty("fov", newFov);
        }

        // Gravity.
        Vector3 vel = getVelocity();
        double vx = vel.getX(), vy = vel.getY(), vz = vel.getZ();

        if (!onFloor) {
            double factor = 3.0 - Math.max(0.0, Math.min(2.0, vy / -MOVEMENT_JUMP_VELOCITY));
            Object gravity = call("get_gravity");
            if (gravity instanceof Vector3) {
                Vector3 g = (Vector3) gravity;
                vy += g.getY() * delta * factor;
            } else {
                vy -= 9.8 * delta * factor;
            }
        }

        // Add horizontal movement.
        vx += movement.getX() * delta;
        vz += movement.getZ() * delta;

        // Apply horizontal friction.
        double friction = onFloor ? MOVEMENT_FRICTION_GROUND : MOVEMENT_FRICTION_AIR;
        double frictionDelta = Math.exp(-friction * delta);
        vx *= frictionDelta;
        vz *= frictionDelta;

        setVelocity(new Vector3(vx, vy, vz));
        move_and_slide();

        // Jumping.
        if (is_on_floor() && (boolean) input.call("is_action_pressed", "jump")) {
            Vector3 curVel = getVelocity();
            setVelocity(new Vector3(curVel.getX(), MOVEMENT_JUMP_VELOCITY, curVel.getZ()));
        }
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (!(inputEvent instanceof org.godot.Godot)) return false;
        org.godot.Godot ev = (org.godot.Godot) inputEvent;

        if ((boolean) ev.call("is_class", "InputEventMouseMotion")) {
            Object mode = input.call("get_mouse_mode");
            // MOUSE_MODE_CAPTURED = 2
            long modeVal = mode instanceof Long ? (Long) mode : 0;
            if (modeVal == 2) {
                Object relObj = ev.getProperty("screen_relative");
                if (relObj instanceof Vector2) {
                    Vector2 rel = (Vector2) relObj;
                    mouseMotion = new Vector2(mouseMotion.x + rel.x, mouseMotion.y + rel.y);
                }
            }
        }
        return false;
    }
}
