package demos.networking.websocket_chat;

import org.godot.Godot;
import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.Signal;
import org.godot.node.Node;

@GodotClass(name = "WSChatWebSocketClient", parent = "Node")
public class WSChatWebSocketClient extends Node {

    @Export
    public String[] handshakeHeaders = new String[0];

    @Export
    public String[] supportedProtocols = new String[0];

    private Godot socket;
    private long lastState = 3L; // STATE_CLOSED

    @Signal
    public void connected_to_server() {}

    @Signal
    public void connection_closed() {}

    @Signal
    public void message_received() {}

    @Override
    public void _ready() {
        socket = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("WebSocketPeer");
        lastState = (long) socket.call("get_ready_state");
    }

    public long connectToUrl(String url) {
        socket.setProperty("supported_protocols", supportedProtocols);
        socket.setProperty("handshake_headers", handshakeHeaders);

        long err = (long) socket.call("connect_to_url", url, (Object) null);
        if (err != 0) return err;

        lastState = (long) socket.call("get_ready_state");
        return 0; // OK
    }

    public long send(String message) {
        return (long) socket.call("send_text", message);
    }

    public Object getMessage() {
        if ((long) socket.call("get_available_packet_count") < 1) return null;
        Object pkt = socket.call("get_packet");
        if ((boolean) socket.call("was_string_packet")) {
            return new String((byte[]) pkt);
        }
        return call("bytes_to_var", pkt);
    }

    public void close(int code, String reason) {
        socket.call("close", code, reason);
        lastState = (long) socket.call("get_ready_state");
    }

    public void closeConnection() {
        close(1000, "");
    }

    public void clear() {
        socket = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("WebSocketPeer");
        lastState = (long) socket.call("get_ready_state");
    }

    public Godot getSocket() {
        return socket;
    }

    @Override
    public void _process(double delta) {
        long currentState = (long) socket.call("get_ready_state");
        if (currentState != 3L) { // Not STATE_CLOSED
            socket.call("poll");
        }

        long state = (long) socket.call("get_ready_state");

        if (lastState != state) {
            lastState = state;
            if (state == 1L) { // STATE_OPEN
                call("emit_signal", "connected_to_server");
            } else if (state == 3L) { // STATE_CLOSED
                call("emit_signal", "connection_closed");
            }
        }

        while ((long) socket.call("get_ready_state") == 1L && (long) socket.call("get_available_packet_count") > 0) {
            Object msg = getMessage();
            call("emit_signal", "message_received", msg);
        }
    }
}
