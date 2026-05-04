package demos.networking.websocket_multiplayer;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.Control;

@GodotClass(name = "WSMPMain", parent = "Control")
public class WSMPMain extends Control {

    private static final int DEF_PORT = 8080;

    private Godot hostBtn;
    private Godot connectBtn;
    private Godot disconnectBtn;
    private Godot nameEdit;
    private Godot hostEdit;
    private Godot game;
    private Godot peer;

    @Override
    public void _ready() {
        hostBtn = (Godot) call("get_node", "Panel/VBoxContainer/HBoxContainer2/HBoxContainer/Host");
        connectBtn = (Godot) call("get_node", "Panel/VBoxContainer/HBoxContainer2/HBoxContainer/Connect");
        disconnectBtn = (Godot) call("get_node", "Panel/VBoxContainer/HBoxContainer2/HBoxContainer/Disconnect");
        nameEdit = (Godot) call("get_node", "Panel/VBoxContainer/HBoxContainer/NameEdit");
        hostEdit = (Godot) call("get_node", "Panel/VBoxContainer/HBoxContainer2/Hostname");
        game = (Godot) call("get_node", "Panel/VBoxContainer/Game");

        peer = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("WebSocketMultiplayerPeer");

        Godot mp = (Godot) call("get_multiplayer");
        mp.call("connect", "peer_connected", new Callable(this, "_peer_connected"), 0);
        mp.call("connect", "peer_disconnected", new Callable(this, "_peer_disconnected"), 0);
        mp.call("connect", "server_disconnected", new Callable(this, "_close_network"), 0);
        mp.call("connect", "connection_failed", new Callable(this, "_close_network"), 0);
        mp.call("connect", "connected_to_server", new Callable(this, "_connected"), 0);

        Godot acceptDialog = (Godot) call("get_node", "AcceptDialog");
        Godot label = (Godot) acceptDialog.call("get_label");
        label.setProperty("horizontal_alignment", 1);
        label.setProperty("vertical_alignment", 1);

        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        if ((boolean) os.call("has_environment", "USERNAME")) {
            String username = (String) os.call("get_environment", "USERNAME");
            nameEdit.setProperty("text", username);
        }
    }

    private void startGame() {
        hostBtn.setProperty("disabled", true);
        nameEdit.setProperty("editable", false);
        hostEdit.setProperty("editable", false);
        connectBtn.call("hide");
        disconnectBtn.call("show");
        game.call("start");
    }

    private void stopGame() {
        hostBtn.setProperty("disabled", false);
        nameEdit.setProperty("editable", true);
        hostEdit.setProperty("editable", true);
        disconnectBtn.call("hide");
        connectBtn.call("show");
        game.call("stop");
    }

    @GodotMethod
    public void _close_network() {
        stopGame();
        Godot acceptDialog = (Godot) call("get_node", "AcceptDialog");
        acceptDialog.call("popup_centered");
        Godot okBtn = (Godot) acceptDialog.call("get_ok_button");
        okBtn.call("grab_focus");
        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", null);
        peer.call("close");
    }

    @GodotMethod
    public void _connected() {
        game.call("rpc", "set_player_name", nameEdit.getProperty("text"));
    }

    @GodotMethod
    public void _peer_connected(long id) {
        game.call("on_peer_add", (int) id);
    }

    @GodotMethod
    public void _peer_disconnected(long id) {
        game.call("on_peer_del", (int) id);
    }

    @GodotMethod
    public void _on_Host_pressed() {
        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", null);
        peer.call("create_server", DEF_PORT);
        mp.setProperty("multiplayer_peer", peer);
        game.call("add_player", 1, nameEdit.getProperty("text"));
        startGame();
    }

    @GodotMethod
    public void _on_Disconnect_pressed() {
        _close_network();
    }

    @GodotMethod
    public void _on_Connect_pressed() {
        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", null);
        String hostText = (String) hostEdit.getProperty("text");
        peer.call("create_client", "ws://" + hostText + ":" + DEF_PORT);
        mp.setProperty("multiplayer_peer", peer);
        startGame();
    }
}
