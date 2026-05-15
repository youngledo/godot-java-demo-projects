package demos.audio.device_changer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.AudioStreamPlayer;
import org.godot.node.Button;
import org.godot.node.Control;
import org.godot.node.ItemList;
import org.godot.node.Label;
import org.godot.singleton.AudioServer;

@GodotClass(name = "DeviceChanger", parent = "Control")
public class DeviceChanger extends Control {

    private ItemList itemList;

    @Override
    public void _ready() {
        itemList = getNodeAs("ItemList", ItemList.class);

        AudioServer audioServer = AudioServer.singleton();
        if (itemList != null) {
            for (String device : audioServer.getOutputDeviceList()) {
                itemList.addItem(device);
            }

            String currentDevice = audioServer.getOutputDevice();
            long itemCount = itemList.getItemCount();
            for (int i = 0; i < itemCount; i++) {
                if (currentDevice.equals(itemList.getItemText(i))) {
                    itemList.select(i);
                    break;
                }
            }
        }
    }

    @Override
    public void _process(double delta) {
        AudioServer audioServer = AudioServer.singleton();

        String speakerModeText = "Stereo";
        AudioServer.SpeakerMode speakerMode = audioServer.getSpeakerMode();

        if (speakerMode == AudioServer.SpeakerMode.SPEAKER_SURROUND_31) {
            speakerModeText = "Surround 3.1";
        } else if (speakerMode == AudioServer.SpeakerMode.SPEAKER_SURROUND_51) {
            speakerModeText = "Surround 5.1";
        } else if (speakerMode == AudioServer.SpeakerMode.SPEAKER_SURROUND_71) {
            speakerModeText = "Surround 7.1";
        }

        Label deviceInfo = getNodeAs("DeviceInfo", Label.class);
        if (deviceInfo != null) {
            String device = audioServer.getOutputDevice();
            deviceInfo.setText("Current Device: " + device + "\nSpeaker Mode: " + speakerModeText);
        }
    }

    @GodotMethod
    public void _onButtonButtonDown() {
        if (itemList == null) return;
        for (int idx : itemList.getSelectedItems()) {
            AudioServer.singleton().setOutputDevice(itemList.getItemText(idx));
        }
    }

    @GodotMethod
    public void _onPlayAudioButtonDown() {
        AudioStreamPlayer audioPlayer = getNodeAs("AudioStreamPlayer", AudioStreamPlayer.class);
        Button playAudioBtn = getNodeAs("PlayAudio", Button.class);
        if (audioPlayer == null) return;

        if (audioPlayer.isPlaying()) {
            audioPlayer.stop();
            if (playAudioBtn != null) playAudioBtn.setText("Play Audio");
        } else {
            audioPlayer.play();
            if (playAudioBtn != null) playAudioBtn.setText("Stop Audio");
        }
    }
}
