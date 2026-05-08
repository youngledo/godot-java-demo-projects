package demos.networking.webrtc_signaling;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;
import org.godot.node.SceneTree;
import org.godot.node.SpinBox;
import org.godot.singleton.OS;

@GodotClass(name = "WebRTCSignalingMain", parent = "Control")
public class WebRTCSignalingMain extends Control {

    @Override
    public void _enterTree() {
        Node clients = getNodeAs("VBoxContainer/Clients", Node.class);
        Godot[] children = (Godot[]) clients.getChildren();
        for (Godot c : children) {
            String path = getPath().toString();
            String cName = (String) c.getProperty("name");
            String nodePath = path + "/VBoxContainer/Clients/" + cName;
            SceneTree tree = getTree();
            tree.setMultiplayer(tree.getMultiplayer(nodePath), nodePath);
        }
    }

    @Override
    public void _ready() {
        String osName = OS.singleton().getName();
        if ("Web".equals(osName)) {
            getNode("VBoxContainer/Signaling").setProperty("visible", false);
        }
    }

    @GodotMethod
    public void OnListenToggled(boolean buttonPressed) {
        WebRTCSignalingServer server = getNodeAs("Server", WebRTCSignalingServer.class);
        if (buttonPressed) {
            SpinBox portSpinBox = getNodeAs("VBoxContainer/Signaling/Port", SpinBox.class);
            int port = (int) portSpinBox.getValue();
            server.listen(port);
        } else {
            server.stop();
        }
    }

    @GodotMethod
    public void OnLinkButtonPressed() {
        OS.singleton().shellOpen("https://github.com/godotengine/webrtc-native/releases");
    }
}
