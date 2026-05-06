package demos.viewport.split_screen_input;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.math.Vector2;
import org.godot.node.OptionButton;
import org.godot.node.SubViewport;
import org.godot.node.VBoxContainer;

@GodotClass(name = "SSSplitScreen", parent = "VBoxContainer")
public class SSSplitScreen extends VBoxContainer {

    private static final String JOYPAD_PREFIX = "Joypad ";

    @Export
    public Vector2 initPosition = new Vector2(0, 0);

    private String[] keyboardNames;
    private int[][] keyboardKeys;
    private int numJoypads;

    private OptionButton opt;
    private SubViewport viewport;
    private SSInputRoutingViewportContainer inputRouter;
    private SSPlayer play;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        opt = (OptionButton) getNode("OptionButton");
        viewport = (SubViewport) getNode("InputRoutingViewportContainer/SubViewport");
        inputRouter = (SSInputRoutingViewportContainer) getNode("InputRoutingViewportContainer");
        play = (SSPlayer) getNode("InputRoutingViewportContainer/SubViewport/Player");
    }

    @Override
    public void _exitTree() {
        if (viewport != null) {
            viewport.setProperty("world_2d", null);
        }
    }

    public void setConfig(String[] names, int[][] keys, int numJoypads, Object world2d, Vector2 position, int index, Color color) {
        this.keyboardNames = names;
        this.keyboardKeys = keys;
        this.numJoypads = numJoypads;

        if (opt == null) {
            opt = (OptionButton) getNode("OptionButton");
        }
        if (play == null) {
            play = (SSPlayer) getNode("InputRoutingViewportContainer/SubViewport/Player");
        }
        if (viewport == null) {
            viewport = (SubViewport) getNode("InputRoutingViewportContainer/SubViewport");
        }
        if (inputRouter == null) {
            inputRouter = (SSInputRoutingViewportContainer) getNode("InputRoutingViewportContainer");
        }

        if (play != null) {
            play.setProperty("position", position);
            play.setProperty("modulate", color);
        }

        if (opt != null) {
            opt.clear();
            for (String name : names) {
                opt.addItem(name);
            }
            for (int i = 0; i < numJoypads; i++) {
                opt.addItem(JOYPAD_PREFIX + (i + 1));
            }
            opt.select(index);
            onOptionButtonItemSelected(index);
        }

        if (viewport != null && world2d != null) {
            viewport.setProperty("world_2d", world2d);
        }
    }

    @GodotMethod
    public void onOptionButtonItemSelected(int index) {
        if (opt == null || inputRouter == null) return;

        String text = (String) opt.call("get_item_text", index);
        if (text != null && text.startsWith(JOYPAD_PREFIX)) {
            String numStr = text.substring(JOYPAD_PREFIX.length());
            int joypadId = Integer.parseInt(numStr) - 1;
            inputRouter.setInputConfig(new int[0], joypadId);
        } else {
            // Find the keyboard index
            int keyIndex = -1;
            for (int i = 0; i < keyboardNames.length; i++) {
                if (keyboardNames[i].equals(text)) {
                    keyIndex = i;
                    break;
                }
            }
            int[] keys = (keyIndex >= 0 && keyIndex < keyboardKeys.length) ? keyboardKeys[keyIndex] : new int[0];
            inputRouter.setInputConfig(keys, -1);
        }
    }
}
