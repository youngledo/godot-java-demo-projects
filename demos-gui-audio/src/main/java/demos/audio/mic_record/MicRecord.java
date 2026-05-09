package demos.audio.mic_record;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.AudioEffect;
import org.godot.node.AudioEffectRecord;
import org.godot.node.AudioStreamPlayer;
import org.godot.node.AudioStreamWAV;
import org.godot.node.Button;
import org.godot.node.Control;
import org.godot.node.Label;
import org.godot.node.LineEdit;
import org.godot.singleton.AudioServer;
import org.godot.singleton.OS;
import org.godot.singleton.ProjectSettings;

@GodotClass(name = "MicRecord", parent = "Control")
public class MicRecord extends Control {

    private AudioEffectRecord effect;
    private AudioStreamWAV recording;

    private boolean stereo = true;
    private int mixRate = 44100;
    private int format = 1;

    @Override
    public void _ready() {
        AudioServer audioServer = AudioServer.singleton();
        int idx = audioServer.getBusIndex("Record");
        AudioEffect busEffect = audioServer.getBusEffect(idx, 0);
        if (busEffect instanceof AudioEffectRecord recordEffect) {
            effect = recordEffect;
        }
    }

    @Override
    public void _exitTree() {
        if (effect != null && effect.isRecordingActive()) {
            effect.setRecordingActive(false);
        }

        AudioStreamPlayer audioPlayer = getNodeAs("AudioStreamPlayer", AudioStreamPlayer.class);
        if (audioPlayer != null) {
            audioPlayer.stop();
            audioPlayer.setStream(null);
        }

        AudioStreamPlayer audioPlayer2 = getNodeAs("AudioStreamPlayer2", AudioStreamPlayer.class);
        if (audioPlayer2 != null) {
            audioPlayer2.stop();
            audioPlayer2.setStream(null);
        }

        recording = null;
        effect = null;
    }

    @GodotMethod
    public void _onRecordButtonPressed() {
        if (effect == null) return;

        if (effect.isRecordingActive()) {
            recording = effect.getRecording();

            Button playButton = getNodeAs("PlayButton", Button.class);
            Button saveButton = getNodeAs("SaveButton", Button.class);
            if (playButton != null) playButton.setDisabled(false);
            if (saveButton != null) saveButton.setDisabled(false);

            effect.setRecordingActive(false);
            if (recording != null) {
                recording.setMixRate(mixRate);
                recording.setFormat(format);
                recording.setStereo(stereo);
            }

            Button recordButton = getNodeAs("RecordButton", Button.class);
            if (recordButton != null) recordButton.setText("Record");

            Label status = getNodeAs("Status", Label.class);
            if (status != null) status.setText("");
        } else {
            Button playButton = getNodeAs("PlayButton", Button.class);
            Button saveButton = getNodeAs("SaveButton", Button.class);
            if (playButton != null) playButton.setDisabled(true);
            if (saveButton != null) saveButton.setDisabled(true);

            effect.setRecordingActive(true);

            Button recordButton = getNodeAs("RecordButton", Button.class);
            if (recordButton != null) recordButton.setText("Stop");

            Label status = getNodeAs("Status", Label.class);
            if (status != null) status.setText("Status: Recording...");
        }
    }

    @GodotMethod
    public void _onPlayButtonPressed() {
        if (recording == null) return;

        long fmt = recording.getFormat();
        String formatStr;
        if (fmt == 0) formatStr = "8-bit uncompressed";
        else if (fmt == 1) formatStr = "16-bit uncompressed";
        else formatStr = "IMA ADPCM compressed";

        long recMixRate = recording.getMixRate();
        boolean recStereo = recording.isStereo();
        byte[] data = recording.getData();

        System.out.println("\nPlaying recording: " + recording);
        System.out.println("Format: " + formatStr);
        System.out.println("Mix rate: " + recMixRate + " Hz");
        System.out.println("Stereo: " + (recStereo ? "Yes" : "No"));
        System.out.println("Size: " + data.length + " bytes");

        AudioStreamPlayer audioPlayer = getNodeAs("AudioStreamPlayer", AudioStreamPlayer.class);
        if (audioPlayer != null) {
            audioPlayer.setStream(recording);
            audioPlayer.play();
        }
    }

    @GodotMethod
    public void _onPlayMusicPressed() {
        AudioStreamPlayer audioPlayer2 = getNodeAs("AudioStreamPlayer2", AudioStreamPlayer.class);
        Button playMusicBtn = getNodeAs("PlayMusic", Button.class);
        if (audioPlayer2 == null) return;

        if (audioPlayer2.isPlaying()) {
            audioPlayer2.stop();
            if (playMusicBtn != null) playMusicBtn.setText("Play Music");
        } else {
            audioPlayer2.play();
            if (playMusicBtn != null) playMusicBtn.setText("Stop Music");
        }
    }

    @GodotMethod
    public void _onSaveButtonPressed() {
        if (recording == null) return;

        Button saveButton = getNodeAs("SaveButton", Button.class);
        LineEdit filenameInput = saveButton != null ? saveButton.getNodeAs("Filename", LineEdit.class) : null;
        String savePath = filenameInput != null ? filenameInput.getText() : "user://record.wav";
        recording.saveToWav(savePath);

        String globalPath = ProjectSettings.singleton().globalizePath(savePath);
        Label status = getNodeAs("Status", Label.class);
        if (status != null) {
            status.setText("Status: Saved WAV file to: " + savePath + "\n(" + globalPath + ")");
        }
    }

    @GodotMethod
    public void _onMixRateOptionButtonItemSelected(int index) {
        switch (index) {
            case 0 -> mixRate = 11025;
            case 1 -> mixRate = 16000;
            case 2 -> mixRate = 22050;
            case 3 -> mixRate = 32000;
            case 4 -> mixRate = 44100;
            case 5 -> mixRate = 48000;
        }
        if (recording != null) {
            recording.setMixRate(mixRate);
        }
    }

    @GodotMethod
    public void _onFormatOptionButtonItemSelected(int index) {
        switch (index) {
            case 0 -> format = 0;
            case 1 -> format = 1;
            case 2 -> format = 2;
        }
        if (recording != null) {
            recording.setFormat(format);
        }
    }

    @GodotMethod
    public void _onStereoCheckButtonToggled(boolean buttonPressed) {
        stereo = buttonPressed;
        if (recording != null) {
            recording.setStereo(stereo);
        }
    }

    @GodotMethod
    public void _onOpenUserFolderButtonPressed() {
        OS.singleton().shellOpen(ProjectSettings.singleton().globalizePath("user://"));
    }
}
