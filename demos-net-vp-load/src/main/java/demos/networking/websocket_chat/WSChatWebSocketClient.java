package demos.networking.websocket_chat;

import org.godot.Godot;
import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.Signal;
import org.godot.node.Node;
import org.godot.node.WebSocketPeer;

@GodotClass(name = "WSChatWebSocketClient", parent = "Node")
public class WSChatWebSocketClient extends Node {

    @Export
    public String[] handshakeHeaders = new String[0];

    @Export
    public String[] supportedProtocols = new String[0];

    private WebSocketPeer socket;
    private long lastState = 3L; // STATE_CLOSED

    @Signal
    public void connectedToServer() {}

    @Signal
    public void connectionClosed() {}

    @Signal
    public void messageReceived() {}

    @Override
    public void _ready() {
        socket = WebSocketPeer.create();
        lastState = socket.getReadyState();
    }

    public long connectToUrl(String url) {
        socket.setSupportedProtocols(supportedProtocols);
        socket.setHandshakeHeaders(handshakeHeaders);

        int err = socket.connectToUrl(url);
        if (err != 0) return err;

        lastState = socket.getReadyState();
        return 0; // OK
    }

    public long send(String message) {
        return socket.sendText(message);
    }

    public Object getMessage() {
        if (socket.getAvailablePacketCount() < 1) return null;
        byte[] pkt = socket.getPacket();
        if (socket.wasStringPacket()) {
            return new String(pkt);
        }
        return bytesToVar(pkt);
    }

    public void close(int code, String reason) {
        socket.close(code, reason);
        lastState = socket.getReadyState();
    }

    public void closeConnection() {
        close(1000, "");
    }

    public void clear() {
        socket = WebSocketPeer.create();
        lastState = socket.getReadyState();
    }

    public WebSocketPeer getSocket() {
        return socket;
    }

    @Override
    public void _process(double delta) {
        int currentState = socket.getReadyState();
        if (currentState != 3L) { // Not STATE_CLOSED
            socket.poll();
        }

        int state = socket.getReadyState();

        if (lastState != state) {
            lastState = state;
            if (state == 1L) { // STATE_OPEN
                emitSignal("connected_to_server");
            } else if (state == 3L) { // STATE_CLOSED
                emitSignal("connection_closed");
            }
        }

        while (socket.getReadyState() == 1L && socket.getAvailablePacketCount() > 0) {
            Object msg = getMessage();
            emitSignal("message_received", msg);
        }
    }
}
