package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.node.Label;
import org.godot.math.Vector3;

@GodotClass(name = "VXDebug", parent = "Label")
public class VXDebug extends Label {

    private org.godot.Godot player;
    private boolean showDebug = false;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        setProperty("visible", false);
    }

    @Override
    public void _process(double delta) {
        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
        if (input != null) {
            Object justPressed = input.call("is_action_just_pressed", "debug");
            if (justPressed instanceof Boolean && (Boolean) justPressed) {
                showDebug = !showDebug;
                setProperty("visible", showDebug);
            }
        }

        if (!showDebug) return;

        if (player == null) {
            player = (org.godot.Godot) call("get_node", "Player");
        }
        if (player == null) return;

        Object posObj = player.getProperty("position");
        Vector3 pos = posObj instanceof Vector3 ? (Vector3) posObj : Vector3.ZERO;

        org.godot.Godot tree = (org.godot.Godot) call("get_tree");
        long fps = 0;
        if (tree != null) {
            Object[] nodes = new Object[0];
            fps = ((Number) tree.call("get_frames_per_second")).longValue();
        }

        String cardinal = getCardinal(pos);
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        String text = String.format("Pos: (%.1f, %.1f, %.1f)  FPS: %d  %s  Mem: %dMB",
                pos.x, pos.y, pos.z, fps, cardinal, mem / 1024 / 1024);
        setProperty("text", text);
    }

    private String getCardinal(Vector3 pos) {
        String[] dirs = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        double angle = Math.atan2(pos.x, pos.z);
        int idx = (int) Math.round(((angle / (2 * Math.PI)) * 8 + 8)) % 8;
        return dirs[idx];
    }
}
