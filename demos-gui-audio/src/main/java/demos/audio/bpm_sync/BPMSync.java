package demos.audio.bpm_sync;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;
import org.godot.node.Node;

/**
 * BPM sync demo - synchronizes visual beat display with audio playback
 * using either system clock or sound clock timing.
 */
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

    // Used by system clock.
    private long timeBegin;
    private double timeDelay;

    @Override
    public void _process(double delta) {
        if (!playing) return;

        org.godot.node.Node player = getNode("Player");
        if (player == null || !((boolean) player.call("playing"))) return;

        double time = 0.0;
        if (syncSource == SYSTEM_CLOCK) {
            // Obtain from ticks.
            org.godot.singleton.Time timeSingleton = org.godot.singleton.Time.singleton();
            long ticksUsec = ((Number) timeSingleton.call("get_ticks_usec")).longValue();
            time = (ticksUsec - timeBegin) / 1000000.0;
            // Compensate.
            time -= timeDelay;
        } else if (syncSource == SOUND_CLOCK) {
            double playbackPos = ((Number) player.call("get_playback_position")).doubleValue();
            org.godot.singleton.AudioServer audioServer = org.godot.singleton.AudioServer.singleton();
            double timeSinceLastMix = ((Number) audioServer.call("get_time_since_last_mix")).doubleValue();
            double outputLatency = ((Number) audioServer.call("get_output_latency")).doubleValue();
            time = playbackPos + timeSinceLastMix - outputLatency + (1.0 / COMPENSATE_HZ) * COMPENSATE_FRAMES;
        }

        int beat = (int) (time * BPM / 60.0);
        int seconds = (int) time;

        org.godot.Godot stream = (org.godot.Godot) player.getProperty("stream");
        int secondsTotal = stream != null ? ((Number) stream.call("get_length")).intValue() : 0;

        org.godot.node.Label label = (org.godot.node.Label) getNode("Label");
        if (label != null) {
            String text = String.format("BEAT: %d/%d TIME: %d:%s / %d:%s",
                    beat % BARS + 1, BARS,
                    seconds / 60, padZeros(seconds % 60, 2),
                    secondsTotal / 60, padZeros(secondsTotal % 60, 2));
            label.setProperty("text", text);
        }
    }

    @GodotMethod
    public void _onPlaySystemPressed() {
        syncSource = SYSTEM_CLOCK;
        org.godot.singleton.Time timeSingleton = org.godot.singleton.Time.singleton();
        timeBegin = ((Number) timeSingleton.call("get_ticks_usec")).longValue();
        org.godot.singleton.AudioServer audioServer = org.godot.singleton.AudioServer.singleton();
        timeDelay = ((Number) audioServer.call("get_time_to_next_mix")).doubleValue()
                + ((Number) audioServer.call("get_output_latency")).doubleValue();
        playing = true;
        org.godot.node.Node player = getNode("Player");
        if (player != null) player.call("play");
    }

    @GodotMethod
    public void _onPlaySoundPressed() {
        syncSource = SOUND_CLOCK;
        playing = true;
        org.godot.node.Node player = getNode("Player");
        if (player != null) player.call("play");
    }

    private static String padZeros(int value, int length) {
        String s = String.valueOf(value);
        while (s.length() < length) {
            s = "0" + s;
        }
        return s;
    }
}
