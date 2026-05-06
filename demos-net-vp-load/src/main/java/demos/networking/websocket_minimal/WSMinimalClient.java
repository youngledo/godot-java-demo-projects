package demos.networking.websocket_minimal;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;

@GodotClass(name = "WSMinimalClient", parent = "Node")
public class WSMinimalClient extends Node {

    private static final String WEBSOCKET_URL = "ws://localhost:9080";

    private Godot socket;

    @Override
    public void _ready() {
        socket = (Godot) org.godot.singleton.ClassDB.singleton().call("instantiate", "WebSocketPeer");

        long err = (long) socket.call("connect_to_url", WEBSOCKET_URL);
        if (err != 0) {
            logMessage("Unable to connect.");
            setProcess(false);
        }
    }

    @Override
    public void _process(double delta) {
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
    }

    @GodotMethod
    public void OnButtonPingPressed() {
        socket.call("send_text", "Ping");
    }

    private void logMessage(String message) {
        String time = (String) call("Time.get_time_string_from_system");
        String formatted = "[color=#aaaaaa] " + time + " |[/color] " + message + "\n";
        Godot textClient = (Godot) getNode("%TextClient");
        textClient.call("append_text", formatted);
    }
}
