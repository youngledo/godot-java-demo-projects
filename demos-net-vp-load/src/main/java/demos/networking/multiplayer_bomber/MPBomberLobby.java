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
        gamestate = (Godot) call("get_node", "/root/gamestate");

        gamestate.call("connect", "connection_failed", new org.godot.core.Callable(this, "_on_connection_failed"));
        gamestate.call("connect", "connection_succeeded", new org.godot.core.Callable(this, "_on_connection_success"));
        gamestate.call("connect", "player_list_changed", new org.godot.core.Callable(this, "refresh_lobby"));
        gamestate.call("connect", "game_ended", new org.godot.core.Callable(this, "_on_game_ended"));
        gamestate.call("connect", "game_error", new org.godot.core.Callable(this, "_on_game_error"));

        // Connect button signals (moved from .tscn [connection] lines)
        Godot startBtn = (Godot) call("get_node", "Players/Start");
        if (startBtn != null) {
            startBtn.call("connect", "pressed", new org.godot.core.Callable(this, "_on_start_pressed"));
        }
        Godot findIpBtn = (Godot) call("get_node", "Players/FindPublicIP");
        if (findIpBtn != null) {
            findIpBtn.call("connect", "pressed", new org.godot.core.Callable(this, "_on_find_public_ip_pressed"));
        }
        Godot hostBtn = (Godot) call("get_node", "Connect/Host");
        if (hostBtn != null) {
            hostBtn.call("connect", "pressed", new org.godot.core.Callable(this, "_on_host_pressed"));
        }
        Godot joinBtn = (Godot) call("get_node", "Connect/Join");
        if (joinBtn != null) {
            joinBtn.call("connect", "pressed", new org.godot.core.Callable(this, "_on_join_pressed"));
        }

        if ((boolean) call("OS.has_environment", "USERNAME")) {
            String username = (String) call("OS.get_environment", "USERNAME");
            Godot nameEdit = (Godot) call("get_node", "Connect/Name");
            nameEdit.setProperty("text", username);
        }
    }

    @GodotMethod
    public void _on_host_pressed() {
        Godot nameEdit = (Godot) call("get_node", "Connect/Name");
        String name = (String) nameEdit.getProperty("text");
        if (name.isEmpty()) {
            Godot errorLabel = (Godot) call("get_node", "Connect/ErrorLabel");
            errorLabel.setProperty("text", "Invalid name!");
            return;
        }

        ((Godot) call("get_node", "Connect")).call("hide");
        ((Godot) call("get_node", "Players")).call("show");
        Godot errorLabel = (Godot) call("get_node", "Connect/ErrorLabel");
        errorLabel.setProperty("text", "");

        gamestate.call("host_game", name);
        Godot window = (Godot) call("get_window");
        String projectName = (String) call("ProjectSettings.get_setting", "application/config/name");
        window.setProperty("title", projectName + ": Server (" + name + ")");
        refresh_lobby();
    }

    @GodotMethod
    public void _on_join_pressed() {
        Godot nameEdit = (Godot) call("get_node", "Connect/Name");
        String name = (String) nameEdit.getProperty("text");
        if (name.isEmpty()) {
            Godot errorLabel = (Godot) call("get_node", "Connect/ErrorLabel");
            errorLabel.setProperty("text", "Invalid name!");
            return;
        }

        Godot ipEdit = (Godot) call("get_node", "Connect/IPAddress");
        String ip = (String) ipEdit.getProperty("text");
        if (!(boolean) ipEdit.call("is_valid_ip_address")) {
            Godot errorLabel = (Godot) call("get_node", "Connect/ErrorLabel");
            errorLabel.setProperty("text", "Invalid IP address!");
            return;
        }

        Godot errorLabel = (Godot) call("get_node", "Connect/ErrorLabel");
        errorLabel.setProperty("text", "");
        ((Godot) call("get_node", "Connect/Host")).setProperty("disabled", true);
        ((Godot) call("get_node", "Connect/Join")).setProperty("disabled", true);

        gamestate.call("join_game", ip, name);
        Godot window = (Godot) call("get_window");
        String projectName = (String) call("ProjectSettings.get_setting", "application/config/name");
        window.setProperty("title", projectName + ": Client (" + name + ")");
    }

    @GodotMethod
    public void _on_connection_success() {
        ((Godot) call("get_node", "Connect")).call("hide");
        ((Godot) call("get_node", "Players")).call("show");
    }

    @GodotMethod
    public void _on_connection_failed() {
        ((Godot) call("get_node", "Connect/Host")).setProperty("disabled", false);
        ((Godot) call("get_node", "Connect/Join")).setProperty("disabled", false);
        Godot errorLabel = (Godot) call("get_node", "Connect/ErrorLabel");
        errorLabel.call("set_text", "Connection failed.");
    }

    @GodotMethod
    public void _on_game_ended() {
        call("show");
        ((Godot) call("get_node", "Connect")).call("show");
        ((Godot) call("get_node", "Players")).call("hide");
        ((Godot) call("get_node", "Connect/Host")).setProperty("disabled", false);
        ((Godot) call("get_node", "Connect/Join")).setProperty("disabled", false);
    }

    @GodotMethod
    public void _on_game_error(String errtxt) {
        Godot errorDialog = (Godot) call("get_node", "ErrorDialog");
        errorDialog.setProperty("dialog_text", errtxt);
        errorDialog.call("popup_centered");
        ((Godot) call("get_node", "Connect/Host")).setProperty("disabled", false);
        ((Godot) call("get_node", "Connect/Join")).setProperty("disabled", false);
    }

    @GodotMethod
    public void refresh_lobby() {
        Object[] playerList = (Object[]) gamestate.call("get_player_list");
        // Sort players
        java.util.Arrays.sort(playerList);
        Godot list = (Godot) call("get_node", "Players/List");
        list.call("clear");
        list.call("add_item", gamestate.getProperty("playerName") + " (you)");
        for (Object p : playerList) {
            list.call("add_item", p);
        }

        Godot startBtn = (Godot) call("get_node", "Players/Start");
        startBtn.setProperty("disabled", !(boolean) call("multiplayer.is_server"));
    }

    @GodotMethod
    public void _on_start_pressed() {
        gamestate.call("begin_game");
    }

    @GodotMethod
    public void _on_find_public_ip_pressed() {
        call("OS.shell_open", "https://icanhazip.com/");
    }
}
