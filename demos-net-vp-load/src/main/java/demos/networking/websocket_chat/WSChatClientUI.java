package demos.networking.websocket_chat;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "WSChatClientUI", parent = "Control")
public class WSChatClientUI extends Control {

    private WSChatWebSocketClient client;
    private Godot logDest;
    private Godot lineEdit;
    private Godot host;

    @Override
    public void _ready() {
        client = (WSChatWebSocketClient) getNode("WebSocketClient");
        logDest = (Godot) getNode("Panel/VBoxContainer/RichTextLabel");
        lineEdit = (Godot) getNode("Panel/VBoxContainer/Send/LineEdit");
        host = (Godot) getNode("Panel/VBoxContainer/Connect/Host");
    }

    private void info(String msg) {
        System.out.println(msg);
        logDest.call("add_text", msg + "\n");
    }

    @GodotMethod
    public void OnWebSocketClientConnectionClosed() {
        Godot ws = client.getSocket();
        long code = (long) ws.call("get_close_code");
        String reason = (String) ws.call("get_close_reason");
        info("Client just disconnected with code: " + code + ", reason: " + reason);
    }

    @GodotMethod
    public void OnWebSocketClientConnectedToServer() {
        Godot ws = client.getSocket();
        String protocol = (String) ws.call("get_selected_protocol");
        info("Client just connected with protocol: " + protocol);
    }

    @GodotMethod
    public void OnWebSocketClientMessageReceived(String message) {
        info(message);
    }

    @GodotMethod
    public void OnSendPressed() {
        String text = (String) lineEdit.getProperty("text");
        if (text == null || text.isEmpty()) return;

        info("Sending message: " + text);
        client.send(text);
        lineEdit.setProperty("text", "");
    }

    @GodotMethod
    public void OnConnectToggled(boolean pressed) {
        if (!pressed) {
            client.closeConnection();
            return;
        }

        String hostText = (String) host.getProperty("text");
        if (hostText == null || hostText.isEmpty()) return;

        info("Connecting to host: " + hostText);
        long err = client.connectToUrl(hostText);
        if (err != 0) {
            info("Error connecting to host: " + hostText);
        }
    }
}
