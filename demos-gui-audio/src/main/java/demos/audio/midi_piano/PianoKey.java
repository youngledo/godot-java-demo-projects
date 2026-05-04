package demos.audio.midi_piano;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

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

        keyColorRect = (Godot) call("get_node", "Key");
        if (keyColorRect != null) {
            startColor = (org.godot.math.Color) keyColorRect.call("get", "color");
        }
        colorTimer = (Godot) call("get_node", "ColorTimer");
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
            keyColorRect.call("set", "color", mixed);
        }

        // Create audio player for this key press.
        Godot audio = org.godot.node.AudioStreamPlayer.create();
        call("add_child", audio);
        Object sample = org.godot.singleton.ResourceLoader.singleton().load("res://piano_keys/A440.wav", "", 1);
        audio.call("set", "stream", sample);
        audio.call("set", "pitch_scale", pitchScale);
        audio.call("play");

        if (colorTimer != null) colorTimer.call("start");

        // Schedule cleanup after 8 seconds.
        Godot tree = (Godot) call("get_tree");
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
            keyColorRect.call("set", "color", startColor);
        }
    }

    @Override
    public void _exitTree() {
        if (colorTimer != null) colorTimer.call("stop");
        // Free any dynamically created AudioStreamPlayer children
        int childCount = ((Number) call("get_child_count")).intValue();
        for (int i = childCount - 1; i >= 0; i--) {
            Godot child = (Godot) call("get_child", i);
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
