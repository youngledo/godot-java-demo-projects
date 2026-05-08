package demos.networking.websocket_chat;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.LineEdit;
import org.godot.node.RichTextLabel;
import org.godot.node.WebSocketPeer;

@GodotClass(name = "WSChatClientUI", parent = "Control")
public class WSChatClientUI extends Control {

    private WSChatWebSocketClient client;
    private RichTextLabel logDest;
    private LineEdit lineEdit;
    private LineEdit host;

    @Override
    public void _ready() {
        client = getNodeAs("WebSocketClient", WSChatWebSocketClient.class);
        logDest = getNodeAs("Panel/VBoxContainer/RichTextLabel", RichTextLabel.class);
        lineEdit = getNodeAs("Panel/VBoxContainer/Send/LineEdit", LineEdit.class);
        host = getNodeAs("Panel/VBoxContainer/Connect/Host", LineEdit.class);
    }

    private void info(String msg) {
        System.out.println(msg);
        logDest.addText(msg + "\n");
    }

    @GodotMethod
    public void OnWebSocketClientConnectionClosed() {
        WebSocketPeer ws = client.getSocket();
        int code = ws.getCloseCode();
        String reason = ws.getCloseReason();
        info("Client just disconnected with code: " + code + ", reason: " + reason);
    }

    @GodotMethod
    public void OnWebSocketClientConnectedToServer() {
        WebSocketPeer ws = client.getSocket();
        String protocol = ws.getSelectedProtocol();
        info("Client just connected with protocol: " + protocol);
    }

    @GodotMethod
    public void OnWebSocketClientMessageReceived(String message) {
        info(message);
    }

    @GodotMethod
    public void OnSendPressed() {
        String text = lineEdit.getText();
        if (text == null || text.isEmpty()) return;

        info("Sending message: " + text);
        client.send(text);
        lineEdit.setText("");
    }

    @GodotMethod
    public void OnConnectToggled(boolean pressed) {
        if (!pressed) {
            client.closeConnection();
            return;
        }

        String hostText = host.getText();
        if (hostText == null || hostText.isEmpty()) return;

        info("Connecting to host: " + hostText);
        long err = client.connectToUrl(hostText);
        if (err != 0) {
            info("Error connecting to host: " + hostText);
        }
    }
}
