package demos.networking.multiplayer_pong;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.Control;

@GodotClass(name = "MPPongLobby", parent = "Control")
public class MPPongLobby extends Control {

    private static final int DEFAULT_PORT = 8910;

    private Godot address;
    private Godot hostButton;
    private Godot joinButton;
    private Godot statusOk;
    private Godot statusFail;
    private Godot portForwardLabel;
    private Godot findPublicIpButton;
    private Godot peer;

    @Override
    public void _ready() {
        address = (Godot) call("get_node", "Address");
        hostButton = (Godot) call("get_node", "HostButton");
        joinButton = (Godot) call("get_node", "JoinButton");
        statusOk = (Godot) call("get_node", "StatusOk");
        statusFail = (Godot) call("get_node", "StatusFail");
        portForwardLabel = (Godot) call("get_node", "PortForward");
        findPublicIpButton = (Godot) call("get_node", "FindPublicIP");

        Godot mp = (Godot) call("get_multiplayer");
        mp.call("connect", "peer_connected", new Callable(this, "_player_connected"), 0);
        mp.call("connect", "peer_disconnected", new Callable(this, "_player_disconnected"), 0);
        mp.call("connect", "connected_to_server", new Callable(this, "_connected_ok"), 0);
        mp.call("connect", "connection_failed", new Callable(this, "_connected_fail"), 0);
        mp.call("connect", "server_disconnected", new Callable(this, "_server_disconnected"), 0);
    }

    @GodotMethod
    public void _player_connected(long id) {
        Godot pongScene = (Godot) call("load", "res://pong.tscn");
        Godot pong = (Godot) pongScene.call("instantiate");
        pong.call("connect", "game_finished", new Callable(this, "_end_game"), 2); // CONNECT_DEFERRED

        Godot tree = (Godot) call("get_tree");
        Godot root = (Godot) tree.call("get_root");
        root.call("add_child", pong, false, 0);
        call("hide");
    }

    @GodotMethod
    public void _player_disconnected(long id) {
        if ((boolean) call("multiplayer.is_server")) {
            _end_game("Client disconnected.");
        } else {
            _end_game("Server disconnected.");
        }
    }

    @GodotMethod
    public void _connected_ok() {
        // Not needed for this project
    }

    @GodotMethod
    public void _connected_fail() {
        setStatus("Couldn't connect.", false);
        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", null);
        hostButton.setProperty("disabled", false);
        joinButton.setProperty("disabled", false);
    }

    @GodotMethod
    public void _server_disconnected() {
        _end_game("Server disconnected.");
    }

    @GodotMethod
    public void _end_game(String withError) {
        if ((boolean) call("has_node", "/root/Pong")) {
            Godot pong = (Godot) call("get_node", "/root/Pong");
            pong.call("free");
            call("show");
        }

        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", null);
        hostButton.setProperty("disabled", false);
        joinButton.setProperty("disabled", false);

        setStatus(withError, false);
    }

    private void setStatus(String text, boolean isOk) {
        if (isOk) {
            statusOk.setProperty("text", text);
            statusFail.setProperty("text", "");
        } else {
            statusOk.setProperty("text", "");
            statusFail.setProperty("text", text);
        }
    }

    @GodotMethod
    public void _on_host_pressed() {
        peer = (Godot) call("ENetMultiplayerPeer.new");
        long err = (long) peer.call("create_server", DEFAULT_PORT, 1);
        if (err != 0) {
            setStatus("Can't host, address in use.", false);
            return;
        }
        Godot host = (Godot) peer.call("get_host");
        host.call("compress", 0); // ENetConnection.COMPRESS_RANGE_CODER

        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", peer);
        hostButton.setProperty("disabled", true);
        joinButton.setProperty("disabled", true);
        setStatus("Waiting for player...", true);

        Godot window = (Godot) call("get_window");
        String name = (String) call("ProjectSettings.get_setting", "application/config/name");
        window.setProperty("title", name + ": Server");

        portForwardLabel.setProperty("visible", true);
        findPublicIpButton.setProperty("visible", true);
    }

    @GodotMethod
    public void _on_join_pressed() {
        String ip = (String) address.call("get_text");
        if (!(boolean) address.call("is_valid_ip_address")) {
            setStatus("IP address is invalid.", false);
            return;
        }

        peer = (Godot) call("ENetMultiplayerPeer.new");
        peer.call("create_client", ip, DEFAULT_PORT);
        Godot host = (Godot) peer.call("get_host");
        host.call("compress", 0); // ENetConnection.COMPRESS_RANGE_CODER
        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", peer);

        setStatus("Connecting...", true);
        Godot window = (Godot) call("get_window");
        String name = (String) call("ProjectSettings.get_setting", "application/config/name");
        window.setProperty("title", name + ": Client");
    }

    @GodotMethod
    public void _on_find_public_ip_pressed() {
        call("OS.shell_open", "https://icanhazip.com/");
    }
}
