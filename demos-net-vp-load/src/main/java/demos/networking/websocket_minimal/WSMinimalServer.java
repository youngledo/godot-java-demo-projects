package demos.networking.websocket_minimal;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;
import org.godot.node.RichTextLabel;
import org.godot.node.TCPServer;
import org.godot.node.WebSocketPeer;
import org.godot.node.StreamPeerTCP;
import org.godot.singleton.Time;

@GodotClass(name = "WSMinimalServer", parent = "Node")
public class WSMinimalServer extends Node {

    private static final int PORT = 9080;

    private TCPServer tcpServer;
    private WebSocketPeer socket;

    @Override
    public void _ready() {
        tcpServer = TCPServer.create();
        socket = WebSocketPeer.create();

        int err = tcpServer.listen(PORT);
        if (err != 0) {
            logMessage("Unable to start server.");
            setProcess(false);
        }
    }

    @Override
    public void _process(double delta) {
        while (tcpServer.isConnectionAvailable()) {
            StreamPeerTCP conn = tcpServer.takeConnection();
            socket.acceptStream(conn);
        }

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
        tcpServer.stop();
    }

    @GodotMethod
    public void OnButtonPongPressed() {
        socket.sendText("Pong");
    }

    private void logMessage(String message) {
        String time = Time.singleton().getTimeStringFromSystem();
        String formatted = "[color=#aaaaaa] " + time + " |[/color] " + message + "\n";
        RichTextLabel textServer = getNodeAs("%TextServer", RichTextLabel.class);
        textServer.appendText(formatted);
    }
}
