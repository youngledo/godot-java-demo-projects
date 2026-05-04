package demos.audio.generator;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.Node;

/**
 * Generator demo - generates audio programmatically using AudioStreamGenerator.
 * Produces a sine wave with adjustable frequency and volume.
 */
@GodotClass(name = "GeneratorDemo", parent = "Node")
public class GeneratorDemo extends Node {

    // Keep the number of samples per second to mix low, as Java is not super fast.
    private double sampleHz = 22050.0;
    private double pulseHz = 440.0;
    private double phase = 0.0;

    // Actual playback stream, assigned in _ready().
    private org.godot.Godot playback;

    private org.godot.Godot player;

    private void fillBuffer() {
        if (playback == null) {
            return;
        }

        double increment = pulseHz / sampleHz;

        int toFill = ((Number) playback.call("get_frames_available")).intValue();
        while (toFill > 0) {
            // Audio frames are stereo.
            double sample = Math.sin(phase * Math.PI * 2.0);
            playback.call("push_frame", new Vector2(sample, sample));
            phase = (phase + increment) % 1.0;
            toFill--;
        }
    }

    @Override
    public void _process(double delta) {
        if (!is_inside_tree() || player == null || playback == null) {
            return;
        }
        fillBuffer();
    }

    @Override
    public void _ready() {
        player = (org.godot.Godot) call("get_node", "Player");
        if (player == null) return;

        // Setting mix rate is only possible before play().
        org.godot.Godot stream = (org.godot.Godot) player.getProperty("stream");
        if (stream != null) {
            stream.setProperty("mix_rate", sampleHz);
        }
        player.call("call_deferred", "play");
        playback = (org.godot.Godot) player.call("call_deferred", "get_stream_playback");
    }

    @GodotMethod
    public void _onFrequencyHSliderValueChanged(double value) {
        if (!is_inside_tree()) {
            return;
        }
        org.godot.Godot frequencyLabel = (org.godot.Godot) call("get_node", "CenterContainer/Frequency/FrequencyLabel");
        if (frequencyLabel != null) {
            frequencyLabel.setProperty("text", String.format("%d Hz", (int) value));
        }
        pulseHz = value;
    }

    @GodotMethod
    public void _onVolumeHSliderValueChanged(double value) {
        if (!is_inside_tree()) {
            return;
        }
        // Use linear_to_db to get a volume slider that matches perceptual human hearing.
        double db = linearToDb(value);
        org.godot.Godot volumeLabel = (org.godot.Godot) call("get_node", "CenterContainer/Volume/VolumeLabel");
        if (volumeLabel != null) {
            volumeLabel.setProperty("text", String.format("%.2f dB", db));
        }
        if (player != null) {
            player.setProperty("volume_db", db);
        }
    }

    @Override
    public void _exitTree() {
        if (player != null) {
            player.call("stop");
        }
        playback = null;
        player = null;
    }

    private static double linearToDb(double linear) {
        return Math.log(Math.max(linear, 0.00001)) / Math.log(10) * 20.0;
    }
}
