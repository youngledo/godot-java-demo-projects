package demos.audio.generator;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.AudioStreamGenerator;
import org.godot.node.AudioStreamGeneratorPlayback;
import org.godot.node.AudioStreamPlayer;
import org.godot.node.Label;
import org.godot.node.Node;
import org.godot.node.Slider;

@GodotClass(name = "GeneratorDemo", parent = "Node")
public class GeneratorDemo extends Node {

    private double sampleHz = 22050.0;
    private double pulseHz = 440.0;
    private double phase = 0.0;

    private AudioStreamGeneratorPlayback playback;
    private AudioStreamPlayer player;

    private void fillBuffer() {
        if (playback == null) return;

        double increment = pulseHz / sampleHz;

        int toFill = playback.getFramesAvailable();
        while (toFill > 0) {
            double sample = Math.sin(phase * Math.PI * 2.0);
            playback.pushFrame(new Vector2(sample, sample));
            phase = (phase + increment) % 1.0;
            toFill--;
        }
    }

    @Override
    public void _process(double delta) {
        if (!isInsideTree() || player == null || playback == null) {
            return;
        }
        fillBuffer();
    }

    @Override
    public void _ready() {
        player = getNodeAs("Player", AudioStreamPlayer.class);
        if (player == null) return;

        if (player.getStream() instanceof AudioStreamGenerator stream) {
            stream.setMixRate(sampleHz);
        }
        player.play();
        if (player.getStreamPlayback() instanceof AudioStreamGeneratorPlayback generatorPlayback) {
            playback = generatorPlayback;
        }
    }

    @GodotMethod
    public void _onFrequencyHSliderValueChanged(double value) {
        if (!isInsideTree()) return;

        Label frequencyLabel = getNodeAs("CenterContainer/Frequency/FrequencyLabel", Label.class);
        if (frequencyLabel != null) {
            frequencyLabel.setText(String.format("%d Hz", (int) value));
        }
        pulseHz = value;
    }

    @GodotMethod
    public void _onVolumeHSliderValueChanged(double value) {
        if (!isInsideTree()) return;

        double db = linearToDb(value);
        Label volumeLabel = getNodeAs("CenterContainer/Volume/VolumeLabel", Label.class);
        if (volumeLabel != null) {
            volumeLabel.setText(String.format("%.2f dB", db));
        }
        if (player != null) {
            player.setVolumeDb(db);
        }
    }

    @Override
    public void _exitTree() {
        if (player != null) {
            player.stop();
        }
        playback = null;
        player = null;
    }

    private static double linearToDb(double linear) {
        return Math.log(Math.max(linear, 0.00001)) / Math.log(10) * 20.0;
    }
}
