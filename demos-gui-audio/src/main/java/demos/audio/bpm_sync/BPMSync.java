package demos.audio.bpm_sync;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.AudioStream;
import org.godot.node.AudioStreamPlayer;
import org.godot.node.Label;
import org.godot.node.Panel;
import org.godot.singleton.AudioServer;
import org.godot.singleton.Time;

@GodotClass(name = "BPMSync", parent = "Panel")
public class BPMSync extends Panel {

    private static final int SYSTEM_CLOCK = 0;
    private static final int SOUND_CLOCK = 1;

    private static final int BPM = 116;
    private static final int BARS = 4;

    private static final int COMPENSATE_FRAMES = 2;
    private static final double COMPENSATE_HZ = 60.0;

    private boolean playing = false;
    private int syncSource = SYSTEM_CLOCK;

    private long timeBegin;
    private double timeDelay;

    @Override
    public void _process(double delta) {
        if (!playing) return;

        AudioStreamPlayer player = getNodeAs("Player", AudioStreamPlayer.class);
        if (player == null || !player.isPlaying()) return;

        double time = 0.0;
        if (syncSource == SYSTEM_CLOCK) {
            long ticksUsec = Time.singleton().getTicksUsec().longValue();
            time = (ticksUsec - timeBegin) / 1000000.0;
            time -= timeDelay;
        } else if (syncSource == SOUND_CLOCK) {
            AudioServer audioServer = AudioServer.singleton();
            time = player.getPlaybackPosition()
                    + audioServer.getTimeSinceLastMix()
                    - audioServer.getOutputLatency()
                    + (1.0 / COMPENSATE_HZ) * COMPENSATE_FRAMES;
        }

        int beat = (int) (time * BPM / 60.0);
        int seconds = (int) time;

        AudioStream stream = player.getStream();
        int secondsTotal = stream != null ? (int) stream.getLength() : 0;

        Label label = getNodeAs("Label", Label.class);
        if (label != null) {
            String text = String.format("BEAT: %d/%d TIME: %d:%s / %d:%s",
                    beat % BARS + 1, BARS,
                    seconds / 60, padZeros(seconds % 60, 2),
                    secondsTotal / 60, padZeros(secondsTotal % 60, 2));
            label.setText(text);
        }
    }

    @GodotMethod
    public void _onPlaySystemPressed() {
        syncSource = SYSTEM_CLOCK;
        timeBegin = Time.singleton().getTicksUsec().longValue();
        AudioServer audioServer = AudioServer.singleton();
        timeDelay = audioServer.getTimeToNextMix() + audioServer.getOutputLatency();
        playing = true;
        play();
    }

    @GodotMethod
    public void _onPlaySoundPressed() {
        syncSource = SOUND_CLOCK;
        playing = true;
        play();
    }

    private void play() {
        AudioStreamPlayer player = getNodeAs("Player", AudioStreamPlayer.class);
        if (player != null) player.play();
    }

    private static String padZeros(int value, int length) {
        String s = String.valueOf(value);
        while (s.length() < length) {
            s = "0" + s;
        }
        return s;
    }
}
