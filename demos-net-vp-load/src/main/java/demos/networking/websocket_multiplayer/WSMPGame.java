package demos.networking.websocket_multiplayer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Button;
import org.godot.node.Control;
import org.godot.node.ItemList;
import org.godot.node.MultiplayerAPI;
import org.godot.node.Texture2D;
import org.godot.singleton.ResourceLoader;

import java.util.ArrayList;
import java.util.List;

@GodotClass(name = "WSMPGame", parent = "Control")
public class WSMPGame extends Control {

    private static final String[] ACTIONS = {"roll", "pass"};

    private ItemList list;
    private Button action;
    private List<Integer> players = new ArrayList<>();
    private int turn = -1;

    @Override
    public void _ready() {
        list = getNodeAs("HBoxContainer/VBoxContainer/ItemList", ItemList.class);
        action = getNodeAs("HBoxContainer/VBoxContainer/Action", Button.class);
    }

    @GodotMethod
    public void _log(String message) {
        org.godot.node.RichTextLabel rtl = getNodeAs("HBoxContainer/RichTextLabel", org.godot.node.RichTextLabel.class);
        rtl.addText(message + "\n");
    }

    @GodotMethod
    public void setPlayerName(String pName) {
        if (!(boolean) isMultiplayerAuthority()) return;
        int sender = getMultiplayer().getRemoteSenderId();
        rpc("update_player_name", sender, pName);
    }

    @GodotMethod
    public void updatePlayerName(int player, String pName) {
        int pos = players.indexOf(player);
        if (pos != -1) {
            list.setItemText(pos, pName);
        }
    }

    @GodotMethod
    public void requestAction(String actionStr) {
        if (!(boolean) isMultiplayerAuthority()) return;
        int sender = getMultiplayer().getRemoteSenderId();
        if (players.get(turn) != sender) {
            rpc("_log", "Someone is trying to cheat! " + sender);
            return;
        }
        boolean validAction = false;
        for (String a : ACTIONS) {
            if (a.equals(actionStr)) { validAction = true; break; }
        }
        if (!validAction) {
            rpc("_log", "Invalid action: " + actionStr);
            return;
        }
        doAction(actionStr);
        nextTurn();
    }

    private void doAction(String actionStr) {
        String playerName = list.getItemText(turn);
        int val = (int) (Math.random() * 100);
        rpc("_log", playerName + ": " + actionStr + "s " + val);
    }

    @GodotMethod
    public void setTurn(int turnVal) {
        this.turn = turnVal;
        if (turnVal >= players.size()) return;

        for (int i = 0; i < players.size(); i++) {
            if (i == turnVal) {
                list.setItemIcon(i, (Texture2D) ResourceLoader.singleton().load("res://img/crown.png"));
            } else {
                list.setItemIcon(i, null);
            }
        }

        MultiplayerAPI mp = getMultiplayer();
        int uniqueId = mp.getUniqueId();
        action.setDisabled(players.get(turnVal) != uniqueId);
    }

    @GodotMethod
    public void delPlayer(int id) {
        int pos = players.indexOf(id);
        if (pos == -1) return;

        players.remove(pos);
        list.removeItem(pos);

        if (turn > pos) turn -= 1;

        if ((boolean) getMultiplayer().isServer()) {
            rpc("set_turn", turn);
        }
    }

    @GodotMethod
    public void addPlayer(int id, String pName) {
        players.add(id);
        if (pName == null || pName.isEmpty() ) {
            list.addItem("... connecting ...", null, false);
        } else {
            list.addItem(pName, null, false);
        }
    }

    public String getPlayerName(int pos) {
        if (pos < list.getItemCount()) {
            return list.getItemText(pos);
        }
        return "Error!";
    }

    private void nextTurn() {
        turn += 1;
        if (turn >= players.size()) turn = 0;
        rpc("set_turn", turn);
    }

    @GodotMethod
    public void start() {
        setTurn(0);
    }

    @GodotMethod
    public void stop() {
        players.clear();
        list.clear();
        turn = 0;
        action.setDisabled(true);
    }

    @GodotMethod
    public void onPeerAdd(int id) {
        if (!(boolean) getMultiplayer().isServer()) return;

        for (int i = 0; i < players.size(); i++) {
            rpcId(id, "add_player", players.get(i), getPlayerName(i));
        }
        rpcId(id, "add_player", id, "");
        rpcId(id, "set_turn", turn);
    }

    @GodotMethod
    public void onPeerDel(int id) {
        if (!(boolean) getMultiplayer().isServer()) return;
        rpc("del_player", id);
    }

    @GodotMethod
    public void OnActionPressed() {
        if ((boolean) getMultiplayer().isServer()) {
            if (turn != 0) return;
            doAction("roll");
            nextTurn();
        } else {
            rpcId(1, "request_action", "roll");
        }
    }
}
