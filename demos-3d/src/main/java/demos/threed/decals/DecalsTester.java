package demos.threed.decals;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.collection.GodotDictionary;
import org.godot.math.Color;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Button;
import org.godot.node.Camera3D;
import org.godot.node.Decal;
import org.godot.node.InputEvent;
import org.godot.node.InputEventMouseButton;
import org.godot.node.InputEventMouseMotion;
import org.godot.node.Label;
import org.godot.node.Node;
import org.godot.node.Node3D;
import org.godot.node.PackedScene;
import org.godot.node.PhysicsDirectSpaceState3D;
import org.godot.node.PhysicsRayQueryParameters3D;
import org.godot.node.Viewport;
import org.godot.node.World3D;
import org.godot.node.WorldEnvironment;
import org.godot.singleton.RenderingServer;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "DecalsTester", parent = "WorldEnvironment")
public class DecalsTester extends WorldEnvironment {

    private static final double ROT_SPEED = 0.003;
    private static final double ZOOM_SPEED = 0.125;
    private static final int MAIN_BUTTONS = 1 | 2 | 4;

    private int testerIndex = 0;
    private double rotX = Math.toRadians(-22.5);
    private double rotY = Math.toRadians(90);
    private double zoom = 1.5;

    private Node testers;
    private Camera3D cameraHolder;
    private Node3D rotationX;
    private Camera3D camera;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        testers = getNode("Testers");
        cameraHolder = getNodeAs("CameraHolder", Camera3D.class);
        rotationX = getNodeAs("CameraHolder/RotationX", Node3D.class);
        camera = getNodeAs("CameraHolder/RotationX/Camera3D", Camera3D.class);

        if (cameraHolder != null) cameraHolder.setRotation(new Vector3(0, rotY, 0));
        if (rotationX != null) rotationX.setRotation(new Vector3(rotX, 0, 0));
        updateGui();
    }

    @Override
    public boolean _unhandledInput(Object inputEvent) {
        if (!(inputEvent instanceof InputEvent event)) return false;

        if (event.isActionPressed("ui_left")) {
            onPreviousPressed();
            return true;
        }
        if (event.isActionPressed("ui_right")) {
            onNextPressed();
            return true;
        }

        if (event.isActionPressed("place_decal")) {
            placeDecal();
            return true;
        }

        if (event instanceof InputEventMouseButton mouseButton) {
            long buttonIndex = mouseButton.getButtonIndex();
            if (buttonIndex == 4) zoom -= ZOOM_SPEED;
            else if (buttonIndex == 5) zoom += ZOOM_SPEED;
            zoom = clamp(zoom, 1.5, 4);
            return true;
        }

        if (event instanceof InputEventMouseMotion mouseMotion) {
            Object buttonMaskObj = mouseMotion.getProperty("button_mask");
            long buttonMask = buttonMaskObj instanceof Number number ? number.longValue() : 0;
            if ((buttonMask & MAIN_BUTTONS) != 0) {
                Vector2 relative = mouseMotion.getScreenRelative();
                rotY -= relative.getX() * ROT_SPEED;
                rotX -= relative.getY() * ROT_SPEED;
                rotX = clamp(rotX, Math.toRadians(-90), 0);
                if (cameraHolder != null) cameraHolder.setRotation(new Vector3(0, rotY, 0));
                if (rotationX != null) rotationX.setRotation(new Vector3(rotX, 0, 0));
                return true;
            }
        }
        return false;
    }

    @Override
    public void _process(double delta) {
        if (testers == null || cameraHolder == null || camera == null) return;
        if (!(testers.getChild(testerIndex) instanceof Node3D currentTester)) return;

        Vector3 holderPos = cameraHolder.getGlobalPosition();
        Vector3 testerPos = currentTester.getGlobalPosition();
        double newZ = lerp(holderPos.getZ(), testerPos.getZ(), 3 * delta);
        cameraHolder.setGlobalPosition(new Vector3(holderPos.getX(), holderPos.getY(), newZ));

        Vector3 camPos = camera.getPosition();
        double newCamZ = lerp(camPos.getZ(), zoom, 10 * delta);
        camera.setPosition(new Vector3(camPos.getX(), camPos.getY(), newCamZ));
    }

    @GodotMethod
    public void onPreviousPressed() {
        testerIndex = Math.max(0, testerIndex - 1);
        updateGui();
    }

    @GodotMethod
    public void onNextPressed() {
        if (testers != null) {
            int count = (int) testers.getChildCount();
            testerIndex = Math.min(testerIndex + 1, count - 1);
        }
        updateGui();
    }

    @GodotMethod
    public void OnDecalFilterModeItemSelected(long index) {
        RenderingServer.singleton().decalsSetFilter(RenderingServer.DecalFilter.fromValue((int) index));
    }

    private void placeDecal() {
        if (camera == null) return;

        Viewport viewport = getViewport();
        if (viewport == null) return;

        Vector2 mousePos = viewport.getMousePosition();
        Vector3 origin = camera.getGlobalPosition();
        Vector3 target = camera.projectPosition(mousePos, 100);
        World3D world3D = viewport.getWorld3d();
        if (world3D == null) return;

        PhysicsDirectSpaceState3D spaceState = world3D.getDirectSpaceState();
        if (spaceState == null) return;

        PhysicsRayQueryParameters3D query = PhysicsRayQueryParameters3D.create(origin, target);
        GodotDictionary result = spaceState.intersectRay(query);
        Object hitPos = result != null ? result.get("position") : null;
        if (!(hitPos instanceof Vector3 position)) return;

        if (!(ResourceLoader.singleton().load("res://decal.tscn") instanceof PackedScene decalScene)) return;
        if (!(decalScene.instantiate() instanceof Node3D decalRoot)) return;

        addChild(decalRoot);
        Decal decal = decalRoot.getNodeAs("Decal", Decal.class);
        if (decal != null) decal.setModulate(new Color(1, 0, 0));
        decalRoot.setPosition(position);
    }

    private void updateGui() {
        if (testers == null) return;
        Node currentTester = testers.getChild(testerIndex);
        if (currentTester == null) return;
        String name = currentTester.getName();

        Label testName = getNodeAs("TestName", Label.class);
        if (testName != null) testName.setText(capitalize(name));
        Button prevBtn = getNodeAs("Previous", Button.class);
        Button nextBtn = getNodeAs("Next", Button.class);
        if (prevBtn != null) prevBtn.setDisabled(testerIndex == 0);
        if (nextBtn != null) {
            int count = (int) testers.getChildCount();
            nextBtn.setDisabled(testerIndex == count - 1);
        }
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double lerp(double from, double to, double w) {
        return from + (to - from) * w;
    }

    private static String capitalize(String s) {
        String[] parts = s.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(" ");
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) sb.append(parts[i].substring(1));
            }
        }
        return sb.toString();
    }
}
