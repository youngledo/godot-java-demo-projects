package demos.viewport.split_screen_input;

import org.godot.annotation.GodotClass;
import org.godot.math.Color;
import org.godot.math.Vector2;
import org.godot.node.Node;
import org.godot.node.SubViewport;

@GodotClass(name = "SSRoot", parent = "Node")
public class SSRoot extends Node {

    // Keyboard options mapped by name
    private static final String[] KEYBOARD_NAMES = {"wasd", "ijkl", "arrows", "numpad"};
    private static final int[][] KEYBOARD_KEYS = {
        {87, 65, 83, 68},     // W, A, S, D
        {73, 74, 75, 76},     // I, J, K, L
        {16777231, 16777233, 16777232, 16777234}, // Left, Right, Up, Down
        {16777348, 16777349, 16777350, 16777352}  // KP_4, KP_5, KP_6, KP_8
    };

    private static final Color[] PLAYER_COLORS = {
        new Color(1, 1, 1, 1),
        new Color(1, 0.56f, 0.01f, 1),
        new Color(0.02f, 1, 0.35f, 1),
        new Color(1, 0.02f, 0.63f, 1)
    };

    private static final int NUM_JOYPADS = 4;

    private SubViewport playArea;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        playArea = (SubViewport) call("get_node", "PlayArea");
        if (playArea == null) return;

        Object world2d = playArea.getProperty("world_2d");

        // The split_screen_demo.tscn has 4 SplitScreen children with known names
        String[] splitNames = {"SplitScreen1", "SplitScreen2", "SplitScreen3", "SplitScreen4"};
        int index = 0;
        for (String name : splitNames) {
            Object child = call("get_node", name);
            if (child instanceof SSSplitScreen) {
                SSSplitScreen splitChild = (SSSplitScreen) child;
                Vector2 position = new Vector2(
                    (index % 2) * 132 + 132,
                    ((int) Math.floor(index / 2.0)) * 132 + 0
                );
                Color color = index < PLAYER_COLORS.length ? PLAYER_COLORS[index] : new Color(1, 1, 1, 1);

                splitChild.setConfig(KEYBOARD_NAMES, KEYBOARD_KEYS, NUM_JOYPADS, world2d, position, index, color);
                index++;
            }
        }
    }
}
