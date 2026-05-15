package demos.audio.midi_piano;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Color;
import org.godot.node.AudioStream;
import org.godot.node.AudioStreamPlayer;
import org.godot.node.ColorRect;
import org.godot.node.Control;
import org.godot.node.Node;
import org.godot.node.SceneTree;
import org.godot.node.SceneTreeTimer;
import org.godot.node.Timer;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "PianoKey", parent = "Control")
public class PianoKey extends Control {

    private double pitchScale;
    private ColorRect keyColorRect;
    private Color startColor;
    private Timer colorTimer;

    private boolean initialized = false;

    private void ensureInitialized() {
        if (initialized) return;
        initialized = true;

        keyColorRect = getNodeAs("Key", ColorRect.class);
        if (keyColorRect != null) {
            startColor = keyColorRect.getColor();
        }
        colorTimer = getNodeAs("ColorTimer", Timer.class);
    }

    @GodotMethod
    public void setup(int pitchIndex) {
        ensureInitialized();
        setName("PianoKey" + pitchIndex);
        double exponent = (pitchIndex - 69.0) / 12.0;
        pitchScale = Math.pow(2, exponent);
    }

    @GodotMethod
    public void activate() {
        ensureInitialized();
        if (keyColorRect != null && startColor != null) {
            Color yellow = new Color(1, 1, 0);
            Color mixed = new Color(
                    (yellow.r + startColor.r) / 2,
                    (yellow.g + startColor.g) / 2,
                    (yellow.b + startColor.b) / 2
            );
            keyColorRect.setColor(mixed);
        }

        AudioStreamPlayer audio = AudioStreamPlayer.create();
        addChild(audio);
        if (ResourceLoader.singleton().load("res://piano_keys/A440.wav", "", ResourceLoader.CacheMode.CACHE_MODE_REUSE) instanceof AudioStream sample) {
            audio.setStream(sample);
        }
        audio.setPitchScale(pitchScale);
        audio.play();

        if (colorTimer != null) colorTimer.start();

        SceneTree tree = getTree();
        if (tree != null) {
            SceneTreeTimer timer = tree.createTimer(8.0);
            if (timer != null) {
                timer.connect("timeout", new Callable(audio, "queue_free"), 0);
            }
        }
    }

    @GodotMethod
    public void deactivate() {
        ensureInitialized();
        if (keyColorRect != null && startColor != null) {
            keyColorRect.setColor(startColor);
        }
    }

    @Override
    public void _exitTree() {
        if (colorTimer != null) colorTimer.stop();
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            Node child = getChild(i);
            if (child instanceof AudioStreamPlayer audio) {
                audio.stop();
                audio.queueFree();
            }
        }
        keyColorRect = null;
        colorTimer = null;
    }
}
