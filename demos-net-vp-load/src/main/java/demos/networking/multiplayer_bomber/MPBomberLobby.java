package demos.networking.multiplayer_bomber;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.builtin.StringExtensions;
import org.godot.node.AcceptDialog;
import org.godot.node.Button;
import org.godot.node.Control;
import org.godot.node.ItemList;
import org.godot.node.Label;
import org.godot.node.LineEdit;
import org.godot.node.Window;
import org.godot.singleton.OS;
import org.godot.singleton.ProjectSettings;

@GodotClass(name = "MPBomberLobby", parent = "Control")
public class MPBomberLobby extends Control {

    private MPBomberGameState gamestate;

    @Override
    public void _ready() {
        gamestate = getNodeAs("/root/gamestate", MPBomberGameState.class);

        gamestate.connect("connection_failed", new org.godot.core.Callable(this, "_on_connection_failed"), 0);
        gamestate.connect("connection_succeeded", new org.godot.core.Callable(this, "_on_connection_success"), 0);
        gamestate.connect("player_list_changed", new org.godot.core.Callable(this, "refresh_lobby"), 0);
        gamestate.connect("game_ended", new org.godot.core.Callable(this, "_on_game_ended"), 0);
        gamestate.connect("game_error", new org.godot.core.Callable(this, "_on_game_error"), 0);

        Button startBtn = getNodeAs("Players/Start", Button.class);
        if (startBtn != null) {
            startBtn.connect("pressed", new org.godot.core.Callable(this, "_on_start_pressed"), 0);
        }
        Button findIpBtn = getNodeAs("Players/FindPublicIP", Button.class);
        if (findIpBtn != null) {
            findIpBtn.connect("pressed", new org.godot.core.Callable(this, "_on_find_public_ip_pressed"), 0);
        }
        Button hostBtn = getNodeAs("Connect/Host", Button.class);
        if (hostBtn != null) {
            hostBtn.connect("pressed", new org.godot.core.Callable(this, "_on_host_pressed"), 0);
        }
        Button joinBtn = getNodeAs("Connect/Join", Button.class);
        if (joinBtn != null) {
            joinBtn.connect("pressed", new org.godot.core.Callable(this, "_on_join_pressed"), 0);
        }

        if (OS.singleton().hasEnvironment("USERNAME")) {
            String username = OS.singleton().getEnvironment("USERNAME");
            LineEdit nameEdit = getNodeAs("Connect/Name", LineEdit.class);
            nameEdit.setText(username);
        }
    }

    @GodotMethod
    public void OnHostPressed() {
        LineEdit nameEdit = getNodeAs("Connect/Name", LineEdit.class);
        String name = nameEdit.getText();
        if (name.isEmpty() ) {
            Label errorLabel = getNodeAs("Connect/ErrorLabel", Label.class);
            errorLabel.setText("Invalid name!");
            return;
        }

        getNode("Connect").setProperty("visible", false);
        getNode("Players").setProperty("visible", true);
        Label errorLabel2 = getNodeAs("Connect/ErrorLabel", Label.class);
        errorLabel2.setText("");

        gamestate.hostGame(name);
        Window window = getWindow();
        String projectName = (String) ProjectSettings.singleton().getSetting("application/config/name");
        window.setTitle(projectName + ": Server (" + name + ")");
        refreshLobby();
    }

    @GodotMethod
    public void OnJoinPressed() {
        LineEdit nameEdit = getNodeAs("Connect/Name", LineEdit.class);
        String name = nameEdit.getText();
        if (name.isEmpty() ) {
            Label errorLabel = getNodeAs("Connect/ErrorLabel", Label.class);
            errorLabel.setText("Invalid name!");
            return;
        }

        LineEdit ipEdit = getNodeAs("Connect/IPAddress", LineEdit.class);
        String ip = ipEdit.getText();
        if (!StringExtensions.isValidIpAddress(ip)) {
            Label errorLabel = getNodeAs("Connect/ErrorLabel", Label.class);
            errorLabel.setText("Invalid IP address!");
            return;
        }

        Label errorLabel = getNodeAs("Connect/ErrorLabel", Label.class);
        errorLabel.setText("");
        getNode("Connect/Host").setProperty("disabled", true);
        getNode("Connect/Join").setProperty("disabled", true);

        gamestate.joinGame(ip, name);
        Window window = getWindow();
        String projectName = (String) ProjectSettings.singleton().getSetting("application/config/name");
        window.setTitle(projectName + ": Client (" + name + ")");
    }

    @GodotMethod
    public void OnConnectionSuccess() {
        getNode("Connect").setProperty("visible", false);
        getNode("Players").setProperty("visible", true);
    }

    @GodotMethod
    public void OnConnectionFailed() {
        getNode("Connect/Host").setProperty("disabled", false);
        getNode("Connect/Join").setProperty("disabled", false);
        Label errorLabel = getNodeAs("Connect/ErrorLabel", Label.class);
        errorLabel.setText("Connection failed.");
    }

    @GodotMethod
    public void OnGameEnded() {
        show();
        getNode("Connect").setProperty("visible", true);
        getNode("Players").setProperty("visible", false);
        getNode("Connect/Host").setProperty("disabled", false);
        getNode("Connect/Join").setProperty("disabled", false);
    }

    @GodotMethod
    public void OnGameError(String errtxt) {
        AcceptDialog errorDialog = getNodeAs("ErrorDialog", AcceptDialog.class);
        errorDialog.setDialogText(errtxt);
        errorDialog.popupCentered();
        getNode("Connect/Host").setProperty("disabled", false);
        getNode("Connect/Join").setProperty("disabled", false);
    }

    @GodotMethod
    public void refreshLobby() {
        Object[] playerList = (Object[]) gamestate.getPlayerList();
        java.util.Arrays.sort(playerList);
        ItemList list = getNodeAs("Players/List", ItemList.class);
        list.clear();
        list.addItem(gamestate.playerName + " (you)");
        for (Object p : playerList) {
            list.addItem(String.valueOf(p));
        }

        Button startBtn = getNodeAs("Players/Start", Button.class);
        startBtn.setDisabled(!getMultiplayer().isServer());
    }

    @GodotMethod
    public void OnStartPressed() {
        gamestate.beginGame();
    }

    @GodotMethod
    public void OnFindPublicIpPressed() {
        OS.singleton().shellOpen("https://icanhazip.com/");
    }
}
