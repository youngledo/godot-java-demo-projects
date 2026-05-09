package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector3;
import org.godot.node.Label;
import org.godot.node.Node3D;
import org.godot.singleton.Engine;
import org.godot.singleton.Input;

@GodotClass(name = "VXDebug", parent = "Label")
public class VXDebug extends Label {

    private Node3D player;
    private boolean showDebug = false;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        setVisible(false);
    }

    @Override
    public void _process(double delta) {
        Input input = Input.singleton();
        if (input != null && input.isActionJustPressed("debug")) {
            showDebug = !showDebug;
            setVisible(showDebug);
        }

        if (!showDebug) return;

        if (player == null) {
            player = getNodeAs("Player", Node3D.class);
        }
        if (player == null) return;

        Vector3 pos = player.getPosition();
        long fps = Math.round(Engine.singleton().getFramesPerSecond());
        String cardinal = getCardinal(pos);
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        String text = String.format("Pos: (%.1f, %.1f, %.1f)  FPS: %d  %s  Mem: %dMB",
                pos.x, pos.y, pos.z, fps, cardinal, mem / 1024 / 1024);
        setText(text);
    }

    private String getCardinal(Vector3 pos) {
        String[] dirs = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        double angle = Math.atan2(pos.x, pos.z);
        int idx = (int) Math.round(((angle / (2 * Math.PI)) * 8 + 8)) % 8;
        return dirs[idx];
    }
}
