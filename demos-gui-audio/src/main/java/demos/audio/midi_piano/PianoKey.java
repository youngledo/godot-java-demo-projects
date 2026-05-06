package demos.audio.midi_piano;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;

/**
 * PianoKey - represents a single key on the piano keyboard.
 * Handles activation (highlighting + playing sound) and deactivation.
 */
@GodotClass(name = "PianoKey", parent = "Control")
public class PianoKey extends Control {

    private double pitchScale;
    private Godot keyColorRect;
    private org.godot.math.Color startColor;
    private Godot colorTimer;

    private boolean initialized = false;

    private void ensureInitialized() {
        if (initialized) return;
        initialized = true;

        keyColorRect = (Godot) getNode("Key");
        if (keyColorRect != null) {
            startColor = (org.godot.math.Color) keyColorRect.getProperty("color");
        }
        colorTimer = (Godot) getNode("ColorTimer");
    }

    @GodotMethod
    public void setup(int pitchIndex) {
        ensureInitialized();
        call("set", "name", "PianoKey" + pitchIndex);
        double exponent = (pitchIndex - 69.0) / 12.0;
        pitchScale = Math.pow(2, exponent);
    }

    @GodotMethod
    public void activate() {
        ensureInitialized();
        if (keyColorRect != null && startColor != null) {
            // (Color.YELLOW + startColor) / 2
            org.godot.math.Color yellow = new org.godot.math.Color(1, 1, 0);
            org.godot.math.Color mixed = new org.godot.math.Color(
                    (yellow.r + startColor.r) / 2,
                    (yellow.g + startColor.g) / 2,
                    (yellow.b + startColor.b) / 2
            );
            keyColorRect.setProperty("color", mixed);
        }

        // Create audio player for this key press.
        Godot audio = org.godot.node.AudioStreamPlayer.create();
        addChild((org.godot.node.Node) audio);
        Object sample = org.godot.singleton.ResourceLoader.singleton().load("res://piano_keys/A440.wav", "", 1);
        audio.setProperty("stream", sample);
        audio.setProperty("pitch_scale", pitchScale);
        audio.call("play");

        if (colorTimer != null) colorTimer.call("start");

        // Schedule cleanup after 8 seconds.
        Godot tree = (Godot) getTree();
        if (tree != null) {
            Godot timer = (Godot) tree.call("create_timer", 8.0);
            if (timer != null) {
                timer.connect("timeout", new org.godot.core.Callable(audio, "queue_free"), 0);
            }
        }
    }

    @GodotMethod
    public void deactivate() {
        ensureInitialized();
        if (keyColorRect != null && startColor != null) {
            keyColorRect.setProperty("color", startColor);
        }
    }

    @Override
    public void _exitTree() {
        if (colorTimer != null) colorTimer.call("stop");
        // Free any dynamically created AudioStreamPlayer children
        int childCount = ((Number) getChildCount()).intValue();
        for (int i = childCount - 1; i >= 0; i--) {
            Godot child = (Godot) getChild(i);
            if (child != null) {
                String cls = (String) child.call("get_class");
                if ("AudioStreamPlayer".equals(cls)) {
                    child.call("stop");
                    child.call("queue_free");
                }
            }
        }
        keyColorRect = null;
        colorTimer = null;
    }
}
