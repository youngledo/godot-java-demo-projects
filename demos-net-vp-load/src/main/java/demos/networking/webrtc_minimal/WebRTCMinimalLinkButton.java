package demos.networking.webrtc_minimal;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.LinkButton;

@GodotClass(name = "WebRTCMinimalLinkButton", parent = "LinkButton")
public class WebRTCMinimalLinkButton extends LinkButton {

    @GodotMethod
    public void OnLinkButtonPressed() {
        call("OS.shell_open", "https://github.com/godotengine/webrtc-native/releases");
    }
}
