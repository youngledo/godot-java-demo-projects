package demos.audio.device_changer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;

/**
 * Device changer demo - lists and switches audio output devices.
 */
@GodotClass(name = "DeviceChanger", parent = "Control")
public class DeviceChanger extends Control {

    private org.godot.node.ItemList itemList;

    @Override
    public void _ready() {
        itemList = (org.godot.node.ItemList) getNode("ItemList");

        org.godot.singleton.AudioServer audioServer = org.godot.singleton.AudioServer.singleton();

        // Populate device list.
        Object[] deviceList = (Object[]) audioServer.call("get_output_device_list");
        for (Object device : deviceList) {
            if (itemList != null) itemList.addItem((String) device);
        }

        // Select current device.
        String currentDevice = (String) audioServer.call("get_output_device");
        if (itemList != null) {
            int itemCount = ((Number) itemList.getItemCount()).intValue();
            for (int i = 0; i < itemCount; i++) {
                String itemText = (String) itemList.getItemText(i);
                if (currentDevice.equals(itemText)) {
                    itemList.select(i);
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

        org.godot.node.Control deviceInfo = (org.godot.node.Control) getNode("DeviceInfo");
        if (deviceInfo != null) {
            String device = (String) audioServer.call("get_output_device");
            deviceInfo.setProperty("text", "Current Device: " + device + "\nSpeaker Mode: " + speakerModeText);
        }
    }

    @GodotMethod
    public void _onButtonButtonDown() {
        if (itemList == null) return;
        int[] selectedItems = (int[]) itemList.call("get_selected_items");
        for (int idx : selectedItems) {
            String device = (String) itemList.call("get_item_text", idx);
            org.godot.singleton.AudioServer.singleton().setOutputDevice(device);
        }
    }

    @GodotMethod
    public void _onPlayAudioButtonDown() {
        org.godot.node.AudioStreamPlayer audioPlayer = (org.godot.node.AudioStreamPlayer) getNode("AudioStreamPlayer");
        org.godot.node.Node playAudioBtn = getNode("PlayAudio");
        if (audioPlayer == null) return;

        if ((boolean) audioPlayer.call("playing")) {
            audioPlayer.stop();
            if (playAudioBtn != null) playAudioBtn.setProperty("text", "Play Audio");
        } else {
            audioPlayer.play();
            if (playAudioBtn != null) playAudioBtn.setProperty("text", "Stop Audio");
        }
    }
}
