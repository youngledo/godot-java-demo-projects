package demos.networking.websocket_chat;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

import java.util.Map;

@GodotClass(name = "WSChatServerUI", parent = "Control")
public class WSChatServerUI extends Control {

    private Godot server;
    private Godot logDest;
    private Godot lineEdit;
    private Godot listenPort;

    @Override
    public void _ready() {
        server = (Godot) call("get_node", "WebSocketServer");
        logDest = (Godot) call("get_node", "Panel/VBoxContainer/RichTextLabel");
        lineEdit = (Godot) call("get_node", "Panel/VBoxContainer/Send/LineEdit");
        listenPort = (Godot) call("get_node", "Panel/VBoxContainer/Connect/Port");
    }

    private void info(String msg) {
        System.out.println(msg);
        logDest.call("add_text", msg + "\n");
    }

    @GodotMethod
    public void _on_web_socket_server_client_connected(long peerId) {
        Godot peersDict = (Godot) server.call("get", "peers");
        Godot peer = (Godot) peersDict.call("get", (int) peerId);
        String protocol = (String) peer.call("get_selected_protocol");
        info("Remote client connected: " + peerId + ". Protocol: " + protocol);
        server.call("send", -(int) peerId, "[" + peerId + "] connected");
    }

    @GodotMethod
    public void _on_web_socket_server_client_disconnected(long peerId) {
        info("Remote client disconnected: " + peerId);
        server.call("send", -(int) peerId, "[" + peerId + "] disconnected");
    }

    @GodotMethod
    public void _on_web_socket_server_message_received(long peerId, String message) {
        info("Server received data from peer " + peerId + ": " + message);
        server.call("send", -(int) peerId, "[" + peerId + "] Says: " + message);
    }

    @GodotMethod
    public void _on_send_pressed() {
        String text = (String) lineEdit.getProperty("text");
        if (text.isEmpty()) return;

        info("Sending message: " + text);
        server.call("send", 0, "Server says: " + text);
        lineEdit.setProperty("text", "");
    }

    @GodotMethod
    public void _on_listen_toggled(boolean pressed) {
        if (!pressed) {
            server.call("stop");
            info("Server stopped");
            return;
        }

        int port = (int) (long) listenPort.call("get_value");
        long err = (long) server.call("listen", port);
        if (err != 0) {
            info("Error listening on port " + port);
            return;
        }
        info("Listening on port " + port);
    }
}
