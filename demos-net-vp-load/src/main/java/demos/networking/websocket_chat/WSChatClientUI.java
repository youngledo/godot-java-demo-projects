package demos.networking.websocket_chat;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "WSChatClientUI", parent = "Control")
public class WSChatClientUI extends Control {

    private Godot client;
    private Godot logDest;
    private Godot lineEdit;
    private Godot host;

    @Override
    public void _ready() {
        client = (Godot) call("get_node", "WebSocketClient");
        logDest = (Godot) call("get_node", "Panel/VBoxContainer/RichTextLabel");
        lineEdit = (Godot) call("get_node", "Panel/VBoxContainer/Send/LineEdit");
        host = (Godot) call("get_node", "Panel/VBoxContainer/Connect/Host");
    }

    private void info(String msg) {
        System.out.println(msg);
        logDest.call("add_text", msg + "\n");
    }

    @GodotMethod
    public void _on_web_socket_client_connection_closed() {
        Godot ws = (Godot) client.call("get_socket");
        long code = (long) ws.call("get_close_code");
        String reason = (String) ws.call("get_close_reason");
        info("Client just disconnected with code: " + code + ", reason: " + reason);
    }

    @GodotMethod
    public void _on_web_socket_client_connected_to_server() {
        Godot ws = (Godot) client.call("get_socket");
        String protocol = (String) ws.call("get_selected_protocol");
        info("Client just connected with protocol: " + protocol);
    }

    @GodotMethod
    public void _on_web_socket_client_message_received(String message) {
        info(message);
    }

    @GodotMethod
    public void _on_send_pressed() {
        String text = (String) lineEdit.getProperty("text");
        if (text == null || text.isEmpty()) return;

        info("Sending message: " + text);
        client.call("send", text);
        lineEdit.setProperty("text", "");
    }

    @GodotMethod
    public void _on_connect_toggled(boolean pressed) {
        if (!pressed) {
            client.call("closeConnection");
            return;
        }

        String hostText = (String) host.getProperty("text");
        if (hostText == null || hostText.isEmpty()) return;

        info("Connecting to host: " + hostText);
        long err = (long) client.call("connect_to_url", hostText);
        if (err != 0) {
            info("Error connecting to host: " + hostText);
        }
    }
}
