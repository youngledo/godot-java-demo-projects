package demos.networking.multiplayer_bomber;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "MPBomberLobby", parent = "Control")
public class MPBomberLobby extends Control {

    private Godot gamestate;

    @Override
    public void _ready() {
        gamestate = (Godot) getNode("/root/gamestate");

        gamestate.connect("connection_failed", new org.godot.core.Callable(this, "_on_connection_failed"), 0);
        gamestate.connect("connection_succeeded", new org.godot.core.Callable(this, "_on_connection_success"), 0);
        gamestate.connect("player_list_changed", new org.godot.core.Callable(this, "refresh_lobby"), 0);
        gamestate.connect("game_ended", new org.godot.core.Callable(this, "_on_game_ended"), 0);
        gamestate.connect("game_error", new org.godot.core.Callable(this, "_on_game_error"), 0);

        // Connect button signals (moved from .tscn [connection] lines)
        Godot startBtn = (Godot) getNode("Players/Start");
        if (startBtn != null) {
            startBtn.connect("pressed", new org.godot.core.Callable(this, "_on_start_pressed"), 0);
        }
        Godot findIpBtn = (Godot) getNode("Players/FindPublicIP");
        if (findIpBtn != null) {
            findIpBtn.connect("pressed", new org.godot.core.Callable(this, "_on_find_public_ip_pressed"), 0);
        }
        Godot hostBtn = (Godot) getNode("Connect/Host");
        if (hostBtn != null) {
            hostBtn.connect("pressed", new org.godot.core.Callable(this, "_on_host_pressed"), 0);
        }
        Godot joinBtn = (Godot) getNode("Connect/Join");
        if (joinBtn != null) {
            joinBtn.connect("pressed", new org.godot.core.Callable(this, "_on_join_pressed"), 0);
        }

        if ((boolean) call("OS.has_environment", "USERNAME")) {
            String username = (String) call("OS.get_environment", "USERNAME");
            Godot nameEdit = (Godot) getNode("Connect/Name");
            nameEdit.setProperty("text", username);
        }
    }

    @GodotMethod
    public void OnHostPressed() {
        Godot nameEdit = (Godot) getNode("Connect/Name");
        String name = (String) nameEdit.getProperty("text");
        if (name.isEmpty() ) {
            Godot errorLabel = (Godot) getNode("Connect/ErrorLabel");
            errorLabel.setProperty("text", "Invalid name!");
            return;
        }

        ((Godot) getNode("Connect")).call("hide");
        ((Godot) getNode("Players")).call("show");
        Godot errorLabel = (Godot) getNode("Connect/ErrorLabel");
        errorLabel.setProperty("text", "");

        gamestate.call("host_game", name);
        Godot window = (Godot) getWindow();
        String projectName = (String) call("ProjectSettings.get_setting", "application/config/name");
        window.setProperty("title", projectName + ": Server (" + name + ")");
        refreshLobby();
    }

    @GodotMethod
    public void OnJoinPressed() {
        Godot nameEdit = (Godot) getNode("Connect/Name");
        String name = (String) nameEdit.getProperty("text");
        if (name.isEmpty() ) {
            Godot errorLabel = (Godot) getNode("Connect/ErrorLabel");
            errorLabel.setProperty("text", "Invalid name!");
            return;
        }

        Godot ipEdit = (Godot) getNode("Connect/IPAddress");
        String ip = (String) ipEdit.getProperty("text");
        if (!(boolean) ipEdit.call("is_valid_ip_address")) {
            Godot errorLabel = (Godot) getNode("Connect/ErrorLabel");
            errorLabel.setProperty("text", "Invalid IP address!");
            return;
        }

        Godot errorLabel = (Godot) getNode("Connect/ErrorLabel");
        errorLabel.setProperty("text", "");
        ((Godot) getNode("Connect/Host")).setProperty("disabled", true);
        ((Godot) getNode("Connect/Join")).setProperty("disabled", true);

        gamestate.call("join_game", ip, name);
        Godot window = (Godot) getWindow();
        String projectName = (String) call("ProjectSettings.get_setting", "application/config/name");
        window.setProperty("title", projectName + ": Client (" + name + ")");
    }

    @GodotMethod
    public void OnConnectionSuccess() {
        ((Godot) getNode("Connect")).call("hide");
        ((Godot) getNode("Players")).call("show");
    }

    @GodotMethod
    public void OnConnectionFailed() {
        ((Godot) getNode("Connect/Host")).setProperty("disabled", false);
        ((Godot) getNode("Connect/Join")).setProperty("disabled", false);
        Godot errorLabel = (Godot) getNode("Connect/ErrorLabel");
        errorLabel.call("set_text", "Connection failed.");
    }

    @GodotMethod
    public void OnGameEnded() {
        show();
        ((Godot) getNode("Connect")).call("show");
        ((Godot) getNode("Players")).call("hide");
        ((Godot) getNode("Connect/Host")).setProperty("disabled", false);
        ((Godot) getNode("Connect/Join")).setProperty("disabled", false);
    }

    @GodotMethod
    public void OnGameError(String errtxt) {
        Godot errorDialog = (Godot) getNode("ErrorDialog");
        errorDialog.setProperty("dialog_text", errtxt);
        errorDialog.call("popup_centered");
        ((Godot) getNode("Connect/Host")).setProperty("disabled", false);
        ((Godot) getNode("Connect/Join")).setProperty("disabled", false);
    }

    @GodotMethod
    public void refreshLobby() {
        Object[] playerList = (Object[]) gamestate.call("get_player_list");
        // Sort players
        java.util.Arrays.sort(playerList);
        Godot list = (Godot) getNode("Players/List");
        list.call("clear");
        list.call("add_item", gamestate.getProperty("playerName") + " (you)");
        for (Object p : playerList) {
            list.call("add_item", p);
        }

        Godot startBtn = (Godot) getNode("Players/Start");
        startBtn.setProperty("disabled", !(boolean) call("multiplayer.is_server"));
    }

    @GodotMethod
    public void OnStartPressed() {
        gamestate.call("begin_game");
    }

    @GodotMethod
    public void OnFindPublicIpPressed() {
        call("OS.shell_open", "https://icanhazip.com/");
    }
}
