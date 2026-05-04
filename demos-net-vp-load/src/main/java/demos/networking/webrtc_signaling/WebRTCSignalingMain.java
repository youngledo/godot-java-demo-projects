package demos.networking.webrtc_signaling;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "WebRTCSignalingMain", parent = "Control")
public class WebRTCSignalingMain extends Control {

    @Override
    public void _enterTree() {
        Godot clients = (Godot) call("get_node", "VBoxContainer/Clients");
        Godot[] children = (Godot[]) clients.call("get_children");
        for (Godot c : children) {
            Godot path = (Godot) call("get_path");
            String cName = (String) c.getProperty("name");
            Godot nodePath = (Godot) call("NodePath", path + "/VBoxContainer/Clients/" + cName);
            Godot tree = (Godot) call("get_tree");
            Godot newMp = (Godot) call("MultiplayerAPI.create_default_interface");
            tree.call("set_multiplayer", newMp, nodePath);
        }
    }

    @Override
    public void _ready() {
        String osName = (String) call("OS.get_name");
        if ("Web".equals(osName)) {
            Godot signaling = (Godot) call("get_node", "VBoxContainer/Signaling");
            signaling.call("hide");
        }
    }

    @GodotMethod
    public void _on_listen_toggled(boolean buttonPressed) {
        Godot server = (Godot) call("get_node", "Server");
        if (buttonPressed) {
            Godot portSpinBox = (Godot) call("get_node", "VBoxContainer/Signaling/Port");
            int port = (int) (long) portSpinBox.call("get_value");
            server.call("listen", port);
        } else {
            server.call("stop");
        }
    }

    @GodotMethod
    public void _on_LinkButton_pressed() {
        call("OS.shell_open", "https://github.com/godotengine/webrtc-native/releases");
    }
}
