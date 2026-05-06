package demos.networking.webrtc_signaling;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "WebRTCSignalingMain", parent = "Control")
public class WebRTCSignalingMain extends Control {

    @Override
    public void _enterTree() {
        Godot clients = (Godot) getNode("VBoxContainer/Clients");
        Godot[] children = (Godot[]) clients.call("get_children");
        for (Godot c : children) {
            Godot path = (Godot) call("get_path");
            String cName = (String) c.getProperty("name");
            Godot nodePath = (Godot) call("NodePath", path + "/VBoxContainer/Clients/" + cName);
            Godot tree = (Godot) getTree();
            Godot newMp = (Godot) call("MultiplayerAPI.create_default_interface");
            tree.call("set_multiplayer", newMp, nodePath);
        }
    }

    @Override
    public void _ready() {
        String osName = (String) call("OS.get_name");
        if ("Web".equals(osName)) {
            Godot signaling = (Godot) getNode("VBoxContainer/Signaling");
            signaling.call("hide");
        }
    }

    @GodotMethod
    public void OnListenToggled(boolean buttonPressed) {
        Godot server = (Godot) getNode("Server");
        if (buttonPressed) {
            Godot portSpinBox = (Godot) getNode("VBoxContainer/Signaling/Port");
            int port = (int) (long) portSpinBox.call("get_value");
            server.call("listen", port);
        } else {
            server.call("stop");
        }
    }

    @GodotMethod
    public void OnLinkButtonPressed() {
        call("OS.shell_open", "https://github.com/godotengine/webrtc-native/releases");
    }
}
