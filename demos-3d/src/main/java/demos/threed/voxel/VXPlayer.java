package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.math.Basis;
import org.godot.math.Rect2;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Camera3D;
import org.godot.node.CameraAttributesPractical;
import org.godot.node.CanvasItem;
import org.godot.node.CharacterBody3D;
import org.godot.node.InputEventMouseMotion;
import org.godot.node.Node;
import org.godot.node.Node3D;
import org.godot.node.RayCast3D;
import org.godot.singleton.Input;

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

    private Node3D head;
    private Camera3D camera;
    private RayCast3D raycast;
    private CameraAttributesPractical cameraAttributes;
    private Node selectedBlockTexture;
    private CanvasItem crosshair;
    private Node3D aimPreview;
    private double neutralFov = 70.0;

    private VXVoxelWorld voxelWorld;
    private VXSettings settings;
    private Input input;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        input = Input.singleton();

        head = getNodeAs("Head", Node3D.class);
        if (head != null) {
            camera = head.getNodeAs("Camera3D", Camera3D.class);
            raycast = head.getNodeAs("RayCast3D", RayCast3D.class);
        }
        if (camera != null) {
            Object attributes = camera.getAttributes();
            if (attributes instanceof CameraAttributesPractical practical) {
                cameraAttributes = practical;
            }
            neutralFov = camera.getFov();
        }
        selectedBlockTexture = getNode("SelectedBlock");
        crosshair = getNodeAs("../PauseMenu/Crosshair", CanvasItem.class);
        aimPreview = getNodeAs("AimPreview", Node3D.class);

        voxelWorld = getNodeAs("../VoxelWorld", VXVoxelWorld.class);
        settings = getNodeAs("/root/Settings", VXSettings.class);

        input.setMouseMode(2);
    }

    @Override
    public void _process(double delta) {
        if (input == null) return;

        double mx = mouseMotion.x;
        double my = Math.max(-1560, Math.min(1560, mouseMotion.y));

        Basis bodyBasis = Basis.fromAxisAngle(Vector3.UP, -mx * 0.001);
        setTransform(new Transform3D(bodyBasis, getPosition()));

        if (head != null) {
            Basis headBasis = Basis.fromAxisAngle(Vector3.RIGHT, -my * 0.001);
            Transform3D headTransform = head.getTransform();
            head.setTransform(new Transform3D(headBasis, headTransform.getOrigin()));
        }

        if (raycast != null && raycast.isColliding()) {
            Vector3 rayPosition = raycast.getCollisionPoint();
            Vector3 rayNormal = raycast.getCollisionNormal();

            if (input.isActionJustPressed("pick_block")) {
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
                    selectedBlock = voxelWorld.getBlockInChunk(cx, cy, cz, sx, sy, sz);
                }
            } else {
                if (input.isActionJustPressed("prev_block")) {
                    selectedBlock -= 1;
                }
                if (input.isActionJustPressed("next_block")) {
                    selectedBlock += 1;
                }
                selectedBlock = ((selectedBlock - 1) % 29 + 29) % 29 + 1;
            }
        }

        if (selectedBlockTexture != null) {
            double[][] uvs = VXChunk.calculateBlockUVs(selectedBlock);
            Object tex = selectedBlockTexture.getProperty("texture");
            if (tex instanceof org.godot.Godot texture) {
                double u0 = uvs[0][0] * 512;
                double v0 = uvs[0][1] * 512;
                texture.setProperty("region", new Rect2(u0, v0, 64, 64));
            }
        }

        boolean crosshairVisible = crosshair != null && crosshair.isVisible();
        boolean rayColliding = raycast != null && raycast.isColliding();

        if (crosshairVisible && rayColliding && raycast != null) {
            if (aimPreview != null) aimPreview.setVisible(true);

            Vector3 rayPosition = raycast.getCollisionPoint();
            Vector3 rayNormal = raycast.getCollisionNormal();

            int rayBlockX = (int) Math.floor(rayPosition.getX() - rayNormal.getX() / 2);
            int rayBlockY = (int) Math.floor(rayPosition.getY() - rayNormal.getY() / 2);
            int rayBlockZ = (int) Math.floor(rayPosition.getZ() - rayNormal.getZ() / 2);

            if (aimPreview != null) {
                aimPreview.setGlobalPosition(new Vector3(rayBlockX + 0.5, rayBlockY + 0.5, rayBlockZ + 0.5));
            }

            boolean breaking = input.isActionJustPressed("break");
            boolean placing = input.isActionJustPressed("place");

            if (breaking != placing) {
                if (breaking && voxelWorld != null) {
                    voxelWorld.setBlockGlobalPosition(rayBlockX, rayBlockY, rayBlockZ, 0);
                } else if (placing && voxelWorld != null) {
                    int placeX = (int) Math.floor(rayPosition.getX() + rayNormal.getX() / 2);
                    int placeY = (int) Math.floor(rayPosition.getY() + rayNormal.getY() / 2);
                    int placeZ = (int) Math.floor(rayPosition.getZ() + rayNormal.getZ() / 2);
                    voxelWorld.setBlockGlobalPosition(placeX, placeY, placeZ, selectedBlock);
                }
            }
        } else {
            if (aimPreview != null) aimPreview.setVisible(false);
        }
    }

    @Override
    public void _physicsProcess(double delta) {
        if (input == null) return;

        if (cameraAttributes != null && settings != null) {
            boolean fogOn = settings.fogEnabled;
            double fogD = settings.fogDistance;
            cameraAttributes.setDofBlurFarEnabled(fogOn);
            cameraAttributes.setDofBlurFarDistance(fogD * 1.5);
            cameraAttributes.setDofBlurFarTransition(fogD * 0.125);
        }

        boolean crouching = input.isActionPressed("crouch");
        boolean sprinting = input.isActionPressed("move_sprint");

        if (head != null) {
            Vector3 headOrigin = head.getPosition();
            double targetY = crouching ? EYE_HEIGHT_CROUCH : EYE_HEIGHT_STAND;
            double newY = headOrigin.getY() + (targetY - headOrigin.getY() * (1.0 - Math.exp(-delta * 16.0)));
            head.setPosition(new Vector3(headOrigin.getX(), newY, headOrigin.getZ()));
        }

        Vector2 mv = input.getVector("move_left", "move_right", "move_forward", "move_back");
        double moveX = mv.x;
        double moveZ = mv.y;

        Transform3D transform = getTransform();
        Basis basis = transform.getBasis();
        Vector3 col0 = new Vector3(basis.xx, basis.xy, basis.xz);
        Vector3 col2 = new Vector3(basis.zx, basis.zy, basis.zz);
        Vector3 movement = col0.mul(moveX).add(col2.mul(moveZ));

        boolean onFloor = isOnFloor();
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

        if (camera != null) {
            double currentFov = camera.getFov();
            double newFov = currentFov + (targetFov - currentFov) * (1.0 - Math.exp(-delta * 10.0));
            camera.setFov(newFov);
        }

        Vector3 vel = getVelocity();
        double vx = vel.getX(), vy = vel.getY(), vz = vel.getZ();

        if (!onFloor) {
            double factor = 3.0 - Math.max(0.0, Math.min(2.0, vy / -MOVEMENT_JUMP_VELOCITY));
            vy += getGravity().getY() * delta * factor;
        }

        vx += movement.getX() * delta;
        vz += movement.getZ() * delta;

        double friction = onFloor ? MOVEMENT_FRICTION_GROUND : MOVEMENT_FRICTION_AIR;
        double frictionDelta = Math.exp(-friction * delta);
        vx *= frictionDelta;
        vz *= frictionDelta;

        setVelocity(new Vector3(vx, vy, vz));
        moveAndSlide();

        if (isOnFloor() && input.isActionPressed("jump")) {
            Vector3 curVel = getVelocity();
            setVelocity(new Vector3(curVel.getX(), MOVEMENT_JUMP_VELOCITY, curVel.getZ()));
        }
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof InputEventMouseMotion event && input.getMouseMode() == 2) {
            Vector2 rel = event.getScreenRelative();
            mouseMotion = new Vector2(mouseMotion.x + rel.x, mouseMotion.y + rel.y);
        }
        return false;
    }
}
