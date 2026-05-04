package demos.audio.device_changer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

/**
 * Device changer demo - lists and switches audio output devices.
 */
@GodotClass(name = "DeviceChanger", parent = "Control")
public class DeviceChanger extends Control {

    private org.godot.Godot itemList;

    @Override
    public void _ready() {
        itemList = (org.godot.Godot) call("get_node", "ItemList");

        org.godot.singleton.AudioServer audioServer = org.godot.singleton.AudioServer.singleton();

        // Populate device list.
        Object[] deviceList = (Object[]) audioServer.call("get_output_device_list");
        for (Object device : deviceList) {
            if (itemList != null) itemList.call("add_item", device);
        }

        // Select current device.
        String currentDevice = (String) audioServer.call("get_output_device");
        if (itemList != null) {
            int itemCount = ((Number) itemList.call("get_item_count")).intValue();
            for (int i = 0; i < itemCount; i++) {
                String itemText = (String) itemList.call("get_item_text", i);
                if (currentDevice.equals(itemText)) {
                    itemList.call("select", i);
                    break;
                }
            }
        }
    }

    @Override
    public void _process(double delta) {
        org.godot.singleton.AudioServer audioServer = org.godot.singleton.AudioServer.singleton();

        String speakerModeText = "Stereo";
        long speakerMode = ((Number) audioServer.call("get_speaker_mode")).longValue();

        if (speakerMode == 1) { // SPEAKER_SURROUND_31
            speakerModeText = "Surround 3.1";
        } else if (speakerMode == 2) { // SPEAKER_SURROUND_51
            speakerModeText = "Surround 5.1";
        } else if (speakerMode == 3) { // SPEAKER_SURROUND_71
            speakerModeText = "Surround 7.1";
        }

        org.godot.Godot deviceInfo = (org.godot.Godot) call("get_node", "DeviceInfo");
        if (deviceInfo != null) {
            String device = (String) audioServer.call("get_output_device");
            deviceInfo.setProperty("text", "Current Device: " + device + "\nSpeaker Mode: " + speakerModeText);
        }
    }

    @GodotMethod
    public void _onButtonButtonDown() {
        if (itemList == null) return;
        Object[] selectedItems = (Object[]) itemList.call("get_selected_items");
        for (Object item : selectedItems) {
            int idx = ((Number) item).intValue();
            String device = (String) itemList.call("get_item_text", idx);
            org.godot.singleton.AudioServer.singleton().call("set_output_device", device);
        }
    }

    @GodotMethod
    public void _onPlayAudioButtonDown() {
        org.godot.Godot audioPlayer = (org.godot.Godot) call("get_node", "AudioStreamPlayer");
        org.godot.Godot playAudioBtn = (org.godot.Godot) call("get_node", "PlayAudio");
        if (audioPlayer == null) return;

        if ((boolean) audioPlayer.call("playing")) {
            audioPlayer.call("stop");
            if (playAudioBtn != null) playAudioBtn.setProperty("text", "Play Audio");
        } else {
            audioPlayer.call("play");
            if (playAudioBtn != null) playAudioBtn.setProperty("text", "Stop Audio");
        }
    }
}
