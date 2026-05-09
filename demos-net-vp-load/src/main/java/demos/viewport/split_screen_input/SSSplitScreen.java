package demos.viewport.split_screen_input;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.math.Vector2;
import org.godot.node.OptionButton;
import org.godot.node.SubViewport;
import org.godot.node.VBoxContainer;
import org.godot.node.World2D;

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

        opt = getNodeAs("OptionButton", OptionButton.class);
        viewport = getNodeAs("InputRoutingViewportContainer/SubViewport", SubViewport.class);
        inputRouter = getNodeAs("InputRoutingViewportContainer", SSInputRoutingViewportContainer.class);
        play = getNodeAs("InputRoutingViewportContainer/SubViewport/Player", SSPlayer.class);
    }

    @Override
    public void _exitTree() {
        if (viewport != null) {
            viewport.setWorld2d(null);
        }
    }

    public void setConfig(String[] names, int[][] keys, int numJoypads, Object world2d, Vector2 position, int index, Color color) {
        this.keyboardNames = names;
        this.keyboardKeys = keys;
        this.numJoypads = numJoypads;

        if (opt == null) {
            opt = getNodeAs("OptionButton", OptionButton.class);
        }
        if (play == null) {
            play = getNodeAs("InputRoutingViewportContainer/SubViewport/Player", SSPlayer.class);
        }
        if (viewport == null) {
            viewport = getNodeAs("InputRoutingViewportContainer/SubViewport", SubViewport.class);
        }
        if (inputRouter == null) {
            inputRouter = getNodeAs("InputRoutingViewportContainer", SSInputRoutingViewportContainer.class);
        }

        if (play != null) {
            play.setPosition(position);
            play.setModulate(color);
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

        if (viewport != null && world2d instanceof World2D world) {
            viewport.setWorld2d(world);
        }
    }

    @GodotMethod
    public void onOptionButtonItemSelected(int index) {
        if (opt == null || inputRouter == null) return;

        String text = opt.getItemText(index);
        if (text != null && text.startsWith(JOYPAD_PREFIX)) {
            String numStr = text.substring(JOYPAD_PREFIX.length());
            int joypadId = Integer.parseInt(numStr) - 1;
            inputRouter.setInputConfig(new int[0], joypadId);
        } else {
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
