package demos.networking.websocket_multiplayer;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.AcceptDialog;
import org.godot.node.Button;
import org.godot.node.Control;
import org.godot.node.LineEdit;
import org.godot.node.MultiplayerAPI;
import org.godot.node.RichTextLabel;
import org.godot.node.SceneTree;
import org.godot.node.WebSocketMultiplayerPeer;
import org.godot.singleton.OS;

@GodotClass(name = "WSMPMain", parent = "Control")
public class WSMPMain extends Control {

    private static final int DEF_PORT = 8080;

    private Button hostBtn;
    private Button connectBtn;
    private Button disconnectBtn;
    private LineEdit nameEdit;
    private LineEdit hostEdit;
    private WSMPGame game;
    private WebSocketMultiplayerPeer peer;

    @Override
    public void _ready() {
        hostBtn = getNodeAs("Panel/VBoxContainer/HBoxContainer2/HBoxContainer/Host", Button.class);
        connectBtn = getNodeAs("Panel/VBoxContainer/HBoxContainer2/HBoxContainer/Connect", Button.class);
        disconnectBtn = getNodeAs("Panel/VBoxContainer/HBoxContainer2/HBoxContainer/Disconnect", Button.class);
        nameEdit = getNodeAs("Panel/VBoxContainer/HBoxContainer/NameEdit", LineEdit.class);
        hostEdit = getNodeAs("Panel/VBoxContainer/HBoxContainer2/Hostname", LineEdit.class);
        game = getNodeAs("Panel/VBoxContainer/Game", WSMPGame.class);

        peer = WebSocketMultiplayerPeer.create();

        MultiplayerAPI mp = getMultiplayer();
        mp.connect("peer_connected", new Callable(this, "_peer_connected"), 0);
        mp.connect("peer_disconnected", new Callable(this, "_peer_disconnected"), 0);
        mp.connect("server_disconnected", new Callable(this, "_close_network"), 0);
        mp.connect("connection_failed", new Callable(this, "_close_network"), 0);
        mp.connect("connected_to_server", new Callable(this, "_connected"), 0);

        AcceptDialog acceptDialog = getNodeAs("AcceptDialog", AcceptDialog.class);
        org.godot.node.Label label = acceptDialog.getLabel();
        label.setHorizontalAlignment(1);
        label.setVerticalAlignment(1);

        OS os = OS.singleton();
        if (os.hasEnvironment("USERNAME")) {
            String username = os.getEnvironment("USERNAME");
            nameEdit.setText(username);
        }
    }

    private void startGame() {
        hostBtn.setDisabled(true);
        nameEdit.setEditable(false);
        hostEdit.setEditable(false);
        connectBtn.setVisible(false);
        disconnectBtn.setVisible(true);
        game.start();
    }

    private void stopGame() {
        hostBtn.setDisabled(false);
        nameEdit.setEditable(true);
        hostEdit.setEditable(true);
        disconnectBtn.setVisible(false);
        connectBtn.setVisible(true);
        game.stop();
    }

    @GodotMethod
    public void CloseNetwork() {
        stopGame();
        AcceptDialog acceptDialog = getNodeAs("AcceptDialog", AcceptDialog.class);
        acceptDialog.popupCentered();
        Button okBtn = acceptDialog.getOkButton();
        okBtn.grabFocus();
        getMultiplayer().setProperty("multiplayer_peer", null);
        peer.close();
    }

    @GodotMethod
    public void _connected() {
        game.rpc("set_player_name", nameEdit.getText());
    }

    @GodotMethod
    public void PeerConnected(long id) {
        game.onPeerAdd((int) id);
    }

    @GodotMethod
    public void PeerDisconnected(long id) {
        game.onPeerDel((int) id);
    }

    @GodotMethod
    public void OnHostPressed() {
        getMultiplayer().setProperty("multiplayer_peer", null);
        peer.createServer(DEF_PORT);
        getMultiplayer().setProperty("multiplayer_peer", peer);
        game.addPlayer(1, nameEdit.getText());
        startGame();
    }

    @GodotMethod
    public void OnDisconnectPressed() {
        CloseNetwork();
    }

    @GodotMethod
    public void OnConnectPressed() {
        getMultiplayer().setProperty("multiplayer_peer", null);
        String hostText = hostEdit.getText();
        peer.createClient("ws://" + hostText + ":" + DEF_PORT);
        getMultiplayer().setProperty("multiplayer_peer", peer);
        startGame();
    }
}
