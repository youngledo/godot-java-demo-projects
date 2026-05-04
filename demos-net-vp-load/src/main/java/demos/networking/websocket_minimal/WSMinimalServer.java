package demos.networking.websocket_minimal;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;

@GodotClass(name = "WSMinimalServer", parent = "Node")
public class WSMinimalServer extends Node {

    private static final int PORT = 9080;

    private Godot tcpServer;
    private Godot socket;

    @Override
    public void _ready() {
        tcpServer = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("TCPServer");
        socket = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("WebSocketPeer");

        long err = (long) tcpServer.call("listen", PORT);
        if (err != 0) {
            logMessage("Unable to start server.");
            call("set_process", false);
        }
    }

    @Override
    public void _process(double delta) {
        while ((boolean) tcpServer.call("is_connection_available")) {
            Godot conn = (Godot) tcpServer.call("take_connection");
            socket.call("accept_stream", conn);
        }

        socket.call("poll");

        long state = (long) socket.call("get_ready_state");
        if (state == 1L) { // WebSocketPeer.STATE_OPEN
            while ((long) socket.call("get_available_packet_count") > 0) {
                byte[] pkt = (byte[]) socket.call("get_packet");
                String msg = new String(pkt);
                logMessage(msg);
            }
        }
    }

    @Override
    public void _exitTree() {
        socket.call("close");
        tcpServer.call("stop");
    }

    @GodotMethod
    public void _on_button_pong_pressed() {
        socket.call("send_text", "Pong");
    }

    private void logMessage(String message) {
        String time = (String) call("Time.get_time_string_from_system");
        String formatted = "[color=#aaaaaa] " + time + " |[/color] " + message + "\n";
        Godot textServer = (Godot) call("get_node", "%TextServer");
        textServer.call("append_text", formatted);
    }
}
