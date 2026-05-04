package demos.audio.mic_record;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

/**
 * Mic record demo - records audio from microphone using AudioEffectRecord,
 * with options for format, mix rate, and stereo settings.
 */
@GodotClass(name = "MicRecord", parent = "Control")
public class MicRecord extends Control {

    private org.godot.Godot effect;
    private org.godot.Godot recording;

    private boolean stereo = true;
    private int mixRate = 44100; // Default mix rate on recordings.
    private int format = 1; // FORMAT_16_BITS = 1, default format on recordings.

    @Override
    public void _ready() {
        org.godot.singleton.AudioServer audioServer = org.godot.singleton.AudioServer.singleton();
        Object idx = audioServer.call("get_bus_index", "Record");
        effect = (org.godot.Godot) audioServer.call("get_bus_effect", idx, 0);
    }

    @Override
    public void _exitTree() {
        // Stop recording if active
        if (effect != null && (boolean) effect.call("is_recording_active")) {
            effect.call("set_recording_active", false);
        }
        // Stop audio players
        org.godot.Godot audioPlayer = (org.godot.Godot) call("get_node", "AudioStreamPlayer");
        if (audioPlayer != null) audioPlayer.call("stop");
        org.godot.Godot audioPlayer2 = (org.godot.Godot) call("get_node", "AudioStreamPlayer2");
        if (audioPlayer2 != null) audioPlayer2.call("stop");
        if (audioPlayer != null) audioPlayer.setProperty("stream", null);
        if (audioPlayer2 != null) audioPlayer2.setProperty("stream", null);
        recording = null;
        effect = null;
    }

    @GodotMethod
    public void _onRecordButtonPressed() {
        if (effect == null) return;

        if ((boolean) effect.call("is_recording_active")) {
            recording = (org.godot.Godot) effect.call("get_recording");

            org.godot.Godot playButton = (org.godot.Godot) call("get_node", "PlayButton");
            org.godot.Godot saveButton = (org.godot.Godot) call("get_node", "SaveButton");
            if (playButton != null) playButton.setProperty("disabled", false);
            if (saveButton != null) saveButton.setProperty("disabled", false);

            effect.call("set_recording_active", false);
            if (recording != null) {
                recording.call("set_mix_rate", mixRate);
                recording.call("set_format", format);
                recording.call("set_stereo", stereo);
            }

            org.godot.Godot recordButton = (org.godot.Godot) call("get_node", "RecordButton");
            if (recordButton != null) recordButton.setProperty("text", "Record");

            org.godot.Godot status = (org.godot.Godot) call("get_node", "Status");
            if (status != null) status.setProperty("text", "");
        } else {
            org.godot.Godot playButton = (org.godot.Godot) call("get_node", "PlayButton");
            org.godot.Godot saveButton = (org.godot.Godot) call("get_node", "SaveButton");
            if (playButton != null) playButton.setProperty("disabled", true);
            if (saveButton != null) saveButton.setProperty("disabled", true);

            effect.call("set_recording_active", true);

            org.godot.Godot recordButton = (org.godot.Godot) call("get_node", "RecordButton");
            if (recordButton != null) recordButton.setProperty("text", "Stop");

            org.godot.Godot status = (org.godot.Godot) call("get_node", "Status");
            if (status != null) status.setProperty("text", "Status: Recording...");
        }
    }

    @GodotMethod
    public void _onPlayButtonPressed() {
        if (recording == null) return;

        int fmt = ((Number) recording.call("get_format")).intValue();
        String formatStr;
        if (fmt == 0) formatStr = "8-bit uncompressed";
        else if (fmt == 1) formatStr = "16-bit uncompressed";
        else formatStr = "IMA ADPCM compressed";

        int recMixRate = ((Number) recording.call("get_mix_rate")).intValue();
        boolean recStereo = (boolean) recording.call("is_stereo");

        byte[] data = (byte[]) recording.call("get_data");
        System.out.println("\nPlaying recording: " + recording);
        System.out.println("Format: " + formatStr);
        System.out.println("Mix rate: " + recMixRate + " Hz");
        System.out.println("Stereo: " + (recStereo ? "Yes" : "No"));
        System.out.println("Size: " + data.length + " bytes");

        org.godot.Godot audioPlayer = (org.godot.Godot) call("get_node", "AudioStreamPlayer");
        if (audioPlayer != null) {
            audioPlayer.setProperty("stream", recording);
            audioPlayer.call("play");
        }
    }

    @GodotMethod
    public void _onPlayMusicPressed() {
        org.godot.Godot audioPlayer2 = (org.godot.Godot) call("get_node", "AudioStreamPlayer2");
        org.godot.Godot playMusicBtn = (org.godot.Godot) call("get_node", "PlayMusic");
        if (audioPlayer2 == null) return;

        if ((boolean) audioPlayer2.call("playing")) {
            audioPlayer2.call("stop");
            if (playMusicBtn != null) playMusicBtn.setProperty("text", "Play Music");
        } else {
            audioPlayer2.call("play");
            if (playMusicBtn != null) playMusicBtn.setProperty("text", "Stop Music");
        }
    }

    @GodotMethod
    public void _onSaveButtonPressed() {
        if (recording == null) return;
        org.godot.Godot saveButton = (org.godot.Godot) call("get_node", "SaveButton");
        org.godot.Godot filenameInput = saveButton != null
                ? (org.godot.Godot) saveButton.call("get_node", "Filename")
                : null;
        String savePath = filenameInput != null ? (String) filenameInput.getProperty("text") : "user://record.wav";
        recording.call("save_to_wav", savePath);

        org.godot.singleton.ProjectSettings projectSettings = org.godot.singleton.ProjectSettings.singleton();
        String globalPath = (String) projectSettings.call("globalize_path", savePath);

        org.godot.Godot status = (org.godot.Godot) call("get_node", "Status");
        if (status != null) {
            status.setProperty("text", "Status: Saved WAV file to: " + savePath + "\n(" + globalPath + ")");
        }
    }

    @GodotMethod
    public void _onMixRateOptionButtonItemSelected(int index) {
        switch (index) {
            case 0: mixRate = 11025; break;
            case 1: mixRate = 16000; break;
            case 2: mixRate = 22050; break;
            case 3: mixRate = 32000; break;
            case 4: mixRate = 44100; break;
            case 5: mixRate = 48000; break;
        }
        if (recording != null) {
            recording.call("set_mix_rate", mixRate);
        }
    }

    @GodotMethod
    public void _onFormatOptionButtonItemSelected(int index) {
        switch (index) {
            case 0: format = 0; break; // FORMAT_8_BITS
            case 1: format = 1; break; // FORMAT_16_BITS
            case 2: format = 2; break; // FORMAT_IMA_ADPCM
        }
        if (recording != null) {
            recording.call("set_format", format);
        }
    }

    @GodotMethod
    public void _onStereoCheckButtonToggled(boolean buttonPressed) {
        stereo = buttonPressed;
        if (recording != null) {
            recording.call("set_stereo", stereo);
        }
    }

    @GodotMethod
    public void _onOpenUserFolderButtonPressed() {
        org.godot.singleton.OS.singleton().call("shell_open",
                org.godot.singleton.ProjectSettings.singleton().call("globalize_path", "user://"));
    }
}
