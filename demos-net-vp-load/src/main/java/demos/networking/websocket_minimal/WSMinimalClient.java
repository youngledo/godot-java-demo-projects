package demos.networking.websocket_minimal;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;
import org.godot.node.RichTextLabel;
import org.godot.node.WebSocketPeer;
import org.godot.singleton.Time;

@GodotClass(name = "WSMinimalClient", parent = "Node")
public class WSMinimalClient extends Node {

    private static final String WEBSOCKET_URL = "ws://localhost:9080";

    private WebSocketPeer socket;

    @Override
    public void _ready() {
        socket = WebSocketPeer.create();

        int err = socket.connectToUrl(WEBSOCKET_URL);
        if (err != 0) {
            logMessage("Unable to connect.");
            setProcess(false);
        }
    }

    @Override
    public void _process(double delta) {
        socket.poll();

        WebSocketPeer.State state = socket.getReadyState();
        if (state == WebSocketPeer.State.STATE_OPEN) {
            while (socket.getAvailablePacketCount() > 0) {
                byte[] pkt = socket.getPacket();
                String msg = new String(pkt);
                logMessage(msg);
            }
        }
    }

    @Override
    public void _exitTree() {
        socket.close();
    }

    @GodotMethod
    public void OnButtonPingPressed() {
        socket.sendText("Ping");
    }

    private void logMessage(String message) {
        String time = Time.singleton().getTimeStringFromSystem();
        String formatted = "[color=#aaaaaa] " + time + " |[/color] " + message + "\n";
        RichTextLabel textClient = getNodeAs("%TextClient", RichTextLabel.class);
        textClient.appendText(formatted);
    }
}
