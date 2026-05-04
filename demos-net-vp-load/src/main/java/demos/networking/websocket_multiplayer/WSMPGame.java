package demos.networking.websocket_multiplayer;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

import java.util.ArrayList;
import java.util.List;

@GodotClass(name = "WSMPGame", parent = "Control")
public class WSMPGame extends Control {

    private static final String[] ACTIONS = {"roll", "pass"};

    private Godot list;
    private Godot action;
    private List<Integer> players = new ArrayList<>();
    private int turn = -1;

    @Override
    public void _ready() {
        list = (Godot) call("get_node", "HBoxContainer/VBoxContainer/ItemList");
        action = (Godot) call("get_node", "HBoxContainer/VBoxContainer/Action");
    }

    @GodotMethod
    public void _log(String message) {
        Godot rtl = (Godot) call("get_node", "HBoxContainer/RichTextLabel");
        rtl.call("add_text", message + "\n");
    }

    @GodotMethod
    public void set_player_name(String pName) {
        if (!(boolean) call("is_multiplayer_authority")) return;
        long sender = (long) call("multiplayer.get_remote_sender_id");
        call("rpc", "update_player_name", (int) sender, pName);
    }

    @GodotMethod
    public void update_player_name(int player, String pName) {
        int pos = players.indexOf(player);
        if (pos != -1) {
            list.call("set_item_text", pos, pName);
        }
    }

    @GodotMethod
    public void request_action(String actionStr) {
        if (!(boolean) call("is_multiplayer_authority")) return;
        long sender = (long) call("multiplayer.get_remote_sender_id");
        if (players.get(turn) != (int) sender) {
            call("rpc", "_log", "Someone is trying to cheat! " + sender);
            return;
        }
        boolean validAction = false;
        for (String a : ACTIONS) {
            if (a.equals(actionStr)) { validAction = true; break; }
        }
        if (!validAction) {
            call("rpc", "_log", "Invalid action: " + actionStr);
            return;
        }
        doAction(actionStr);
        nextTurn();
    }

    private void doAction(String actionStr) {
        String playerName = (String) list.call("get_item_text", turn);
        int val = (int) (Math.random() * 100);
        call("rpc", "_log", playerName + ": " + actionStr + "s " + val);
    }

    @GodotMethod
    public void set_turn(int turnVal) {
        this.turn = turnVal;
        if (turnVal >= players.size()) return;

        for (int i = 0; i < players.size(); i++) {
            if (i == turnVal) {
                list.call("set_item_icon", i, call("preload", "res://img/crown.png"));
            } else {
                list.call("set_item_icon", i, null);
            }
        }

        Godot mp = (Godot) call("get_multiplayer");
        long uniqueId = (long) mp.call("get_unique_id");
        action.setProperty("disabled", players.get(turnVal) != (int) uniqueId);
    }

    @GodotMethod
    public void del_player(int id) {
        int pos = players.indexOf(id);
        if (pos == -1) return;

        players.remove(pos);
        list.call("remove_item", pos);

        if (turn > pos) turn -= 1;

        if ((boolean) call("multiplayer.is_server")) {
            call("rpc", "set_turn", turn);
        }
    }

    @GodotMethod
    public void add_player(int id, String pName) {
        players.add(id);
        if (pName == null || pName.isEmpty()) {
            list.call("add_item", "... connecting ...", null, false);
        } else {
            list.call("add_item", pName, null, false);
        }
    }

    public String getPlayerName(int pos) {
        if (pos < (long) list.call("get_item_count")) {
            return (String) list.call("get_item_text", pos);
        }
        return "Error!";
    }

    private void nextTurn() {
        turn += 1;
        if (turn >= players.size()) turn = 0;
        call("rpc", "set_turn", turn);
    }

    @GodotMethod
    public void start() {
        set_turn(0);
    }

    @GodotMethod
    public void stop() {
        players.clear();
        list.call("clear");
        turn = 0;
        action.setProperty("disabled", true);
    }

    @GodotMethod
    public void on_peer_add(int id) {
        if (!(boolean) call("multiplayer.is_server")) return;

        for (int i = 0; i < players.size(); i++) {
            call("rpc_id", id, "add_player", players.get(i), getPlayerName(i));
        }
        call("rpc", id, "add_player", "");
        call("rpc_id", id, "set_turn", turn);
    }

    @GodotMethod
    public void on_peer_del(int id) {
        if (!(boolean) call("multiplayer.is_server")) return;
        call("rpc", "del_player", id);
    }

    @GodotMethod
    public void _on_Action_pressed() {
        if ((boolean) call("multiplayer.is_server")) {
            if (turn != 0) return;
            doAction("roll");
            nextTurn();
        } else {
            call("rpc_id", 1, "request_action", "roll");
        }
    }
}
