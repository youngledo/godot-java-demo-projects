package demos.networking.websocket_chat;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.LineEdit;
import org.godot.node.RichTextLabel;
import org.godot.node.SpinBox;
import org.godot.node.WebSocketPeer;

@GodotClass(name = "WSChatServerUI", parent = "Control")
public class WSChatServerUI extends Control {

    private WSChatWebSocketServer server;
    private RichTextLabel logDest;
    private LineEdit lineEdit;
    private SpinBox listenPort;

    @Override
    public void _ready() {
        server = getNodeAs("WebSocketServer", WSChatWebSocketServer.class);
        logDest = getNodeAs("Panel/VBoxContainer/RichTextLabel", RichTextLabel.class);
        lineEdit = getNodeAs("Panel/VBoxContainer/Send/LineEdit", LineEdit.class);
        listenPort = getNodeAs("Panel/VBoxContainer/Connect/Port", SpinBox.class);
    }

    private void info(String msg) {
        System.out.println(msg);
        logDest.addText(msg + "\n");
    }

    @GodotMethod
    public void OnWebSocketServerClientConnected(long peerId) {
        WebSocketPeer peer = server.getPeer((int) peerId);
        String protocol = peer != null ? peer.getSelectedProtocol() : "";
        info("Remote client connected: " + peerId + ". Protocol: " + protocol);
        server.send(-(int) peerId, "[" + peerId + "] connected");
    }

    @GodotMethod
    public void OnWebSocketServerClientDisconnected(long peerId) {
        info("Remote client disconnected: " + peerId);
        server.send(-(int) peerId, "[" + peerId + "] disconnected");
    }

    @GodotMethod
    public void OnWebSocketServerMessageReceived(long peerId, String message) {
        info("Server received data from peer " + peerId + ": " + message);
        server.send(-(int) peerId, "[" + peerId + "] Says: " + message);
    }

    @GodotMethod
    public void OnSendPressed() {
        String text = lineEdit.getText();
        if (text.isEmpty()) return;

        info("Sending message: " + text);
        server.send(0, "Server says: " + text);
        lineEdit.setText("");
    }

    @GodotMethod
    public void OnListenToggled(boolean pressed) {
        if (!pressed) {
            server.stop();
            info("Server stopped");
            return;
        }

        int port = (int) listenPort.getValue();
        long err = server.listen(port);
        if (err != 0) {
            info("Error listening on port " + port);
            return;
        }
        info("Listening on port " + port);
    }
}
