package demos.networking.multiplayer_pong;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.builtin.StringExtensions;
import org.godot.core.Callable;
import org.godot.node.Button;
import org.godot.node.Control;
import org.godot.node.ENetMultiplayerPeer;
import org.godot.node.ENetConnection;
import org.godot.node.Label;
import org.godot.node.LineEdit;
import org.godot.node.MultiplayerAPI;
import org.godot.node.Node;
import org.godot.node.PackedScene;
import org.godot.node.SceneTree;
import org.godot.node.Window;
import org.godot.singleton.OS;
import org.godot.singleton.ProjectSettings;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "MPPongLobby", parent = "Control")
public class MPPongLobby extends Control {

    private static final int DEFAULT_PORT = 8910;

    private LineEdit address;
    private Button hostButton;
    private Button joinButton;
    private Label statusOk;
    private Label statusFail;
    private Node portForwardLabel;
    private Node findPublicIpButton;
    private ENetMultiplayerPeer peer;

    @Override
    public void _ready() {
        address = getNodeAs("Address", LineEdit.class);
        hostButton = getNodeAs("HostButton", Button.class);
        joinButton = getNodeAs("JoinButton", Button.class);
        statusOk = getNodeAs("StatusOk", Label.class);
        statusFail = getNodeAs("StatusFail", Label.class);
        portForwardLabel = getNode("PortForward");
        findPublicIpButton = getNode("FindPublicIP");

        MultiplayerAPI mp = getMultiplayer();
        mp.connect("peer_connected", new Callable(this, "_player_connected"), 0);
        mp.connect("peer_disconnected", new Callable(this, "_player_disconnected"), 0);
        mp.connect("connected_to_server", new Callable(this, "_connected_ok"), 0);
        mp.connect("connection_failed", new Callable(this, "_connected_fail"), 0);
        mp.connect("server_disconnected", new Callable(this, "_server_disconnected"), 0);
    }

    @GodotMethod
    public void PlayerConnected(long id) {
        PackedScene pongScene = (PackedScene) ResourceLoader.singleton().load("res://pong.tscn");
        Node pong = pongScene.instantiate();
        pong.connect("game_finished", new Callable(this, "_end_game"), 2);

        SceneTree tree = getTree();
        Node root = tree.getRoot();
        root.addChild(pong);
        hide();
    }

    @GodotMethod
    public void PlayerDisconnected(long id) {
        if (getMultiplayer().isServer()) {
            EndGame("Client disconnected.");
        } else {
            EndGame("Server disconnected.");
        }
    }

    @GodotMethod
    public void ConnectedOk() {}

    @GodotMethod
    public void ConnectedFail() {
        setStatus("Couldn't connect.", false);
        getMultiplayer().setProperty("multiplayer_peer", null);
        hostButton.setDisabled(false);
        joinButton.setDisabled(false);
    }

    @GodotMethod
    public void ServerDisconnected() {
        EndGame("Server disconnected.");
    }

    @GodotMethod
    public void EndGame(String withError) {
        if (hasNode("/root/Pong")) {
            getNode("/root/Pong").free();
            show();
        }

        getMultiplayer().setProperty("multiplayer_peer", null);
        hostButton.setDisabled(false);
        joinButton.setDisabled(false);

        setStatus(withError, false);
    }

    private void setStatus(String text, boolean isOk) {
        if (isOk) {
            statusOk.setText(text);
            statusFail.setText("");
        } else {
            statusOk.setText("");
            statusFail.setText(text);
        }
    }

    @GodotMethod
    public void OnHostPressed() {
        peer = ENetMultiplayerPeer.create();
        int err = peer.createServer(DEFAULT_PORT, 1);
        if (err != 0) {
            setStatus("Can't host, address in use.", false);
            return;
        }
        ENetConnection host = peer.getHost();
        host.compress(0);

        getMultiplayer().setProperty("multiplayer_peer", peer);
        hostButton.setDisabled(true);
        joinButton.setDisabled(true);
        setStatus("Waiting for player...", true);

        Window window = getWindow();
        String name = (String) ProjectSettings.singleton().getSetting("application/config/name");
        window.setTitle(name + ": Server");

        portForwardLabel.setProperty("visible", true);
        findPublicIpButton.setProperty("visible", true);
    }

    @GodotMethod
    public void OnJoinPressed() {
        String ip = address.getText();
        if (!StringExtensions.isValidIpAddress(ip)) {
            setStatus("IP address is invalid.", false);
            return;
        }

        peer = ENetMultiplayerPeer.create();
        peer.createClient(ip, DEFAULT_PORT);
        ENetConnection host = peer.getHost();
        host.compress(0);
        getMultiplayer().setProperty("multiplayer_peer", peer);

        setStatus("Connecting...", true);
        Window window = getWindow();
        String name = (String) ProjectSettings.singleton().getSetting("application/config/name");
        window.setTitle(name + ": Client");
    }

    @GodotMethod
    public void OnFindPublicIpPressed() {
        OS.singleton().shellOpen("https://icanhazip.com/");
    }
}
