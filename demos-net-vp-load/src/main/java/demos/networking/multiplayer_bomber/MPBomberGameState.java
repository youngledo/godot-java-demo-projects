package demos.networking.multiplayer_bomber;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.math.Vector2;
import org.godot.node.Node;

import java.util.HashMap;
import java.util.Map;

@GodotClass(name = "MPBomberGameState", parent = "Node")
public class MPBomberGameState extends Node {

    private static final int DEFAULT_PORT = 10567;
    private static final int MAX_PEERS = 12;

    private Godot peer;
    public String playerName = "The Warrior";
    public Map<Integer, String> players = new HashMap<>();

    @Signal
    public void player_list_changed() {}

    @Signal
    public void connection_succeeded() {}

    @Signal
    public void connection_failed() {}

    @Signal
    public void game_ended() {}

    @Signal
    public void game_error() {}

    @Override
    public void _ready() {
        Godot mp = (Godot) call("get_multiplayer");
        mp.call("connect", "peer_connected", new org.godot.core.Callable(this, "_player_connected"));
        mp.call("connect", "peer_disconnected", new org.godot.core.Callable(this, "_player_disconnected"));
        mp.call("connect", "connected_to_server", new org.godot.core.Callable(this, "_connected_ok"));
        mp.call("connect", "connection_failed", new org.godot.core.Callable(this, "_connected_fail"));
        mp.call("connect", "server_disconnected", new org.godot.core.Callable(this, "_server_disconnected"));
    }

    @GodotMethod
    public void _player_connected(long id) {
        call("rpc_id", (int) id, "register_player", playerName);
    }

    @GodotMethod
    public void _player_disconnected(long id) {
        if ((boolean) call("has_node", "/root/World")) {
            if ((boolean) call("multiplayer.is_server")) {
                call("emit_signal", "game_error", "Player " + players.get((int) id) + " disconnected");
                end_game();
            }
        } else {
            unregisterPlayer((int) id);
        }
    }

    @GodotMethod
    public void _connected_ok() {
        call("emit_signal", "connection_succeeded");
    }

    @GodotMethod
    public void _server_disconnected() {
        call("emit_signal", "game_error", "Server disconnected");
        end_game();
    }

    @GodotMethod
    public void _connected_fail() {
        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", null);
        call("emit_signal", "connection_failed");
    }

    @GodotMethod
    public void register_player(String newPlayerName) {
        long id = (long) call("multiplayer.get_remote_sender_id");
        players.put((int) id, newPlayerName);
        call("emit_signal", "player_list_changed");
    }

    private void unregisterPlayer(int id) {
        players.remove(id);
        call("emit_signal", "player_list_changed");
    }

    @GodotMethod
    public void load_world() {
        Godot worldScene = (Godot) call("load", "res://world.tscn");
        Godot world = (Godot) worldScene.call("instantiate");
        Godot tree = (Godot) call("get_tree");
        Godot root = (Godot) tree.call("get_root");
        root.call("add_child", world, false, 0);
        Godot lobby = (Godot) root.call("get_node", "Lobby");
        lobby.call("hide");

        Godot score = (Godot) world.call("get_node", "Score");
        long uniqueId = (long) call("multiplayer.get_unique_id");
        score.call("add_player", (int) uniqueId, playerName);
        for (int pn : players.keySet()) {
            score.call("add_player", pn, players.get(pn));
        }

        tree.setProperty("paused", false);
    }

    @GodotMethod
    public void host_game(String newPlayerName) {
        playerName = newPlayerName;
        peer = (Godot) call("ENetMultiplayerPeer.new");
        peer.call("create_server", DEFAULT_PORT, MAX_PEERS);
        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", peer);
    }

    @GodotMethod
    public void join_game(String ip, String newPlayerName) {
        playerName = newPlayerName;
        peer = (Godot) call("ENetMultiplayerPeer.new");
        peer.call("create_client", ip, DEFAULT_PORT);
        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", peer);
    }

    @GodotMethod
    public Object get_player_list() {
        return players.values().toArray();
    }

    @GodotMethod
    public void begin_game() {
        assert (boolean) call("multiplayer.is_server");
        call("rpc", "load_world");

        Godot tree2 = (Godot) call("get_tree");
        Godot root = (Godot) tree2.call("get_root");
        Godot world = (Godot) root.call("get_node", "World");
        Godot playerScene = (Godot) call("load", "res://player.tscn");

        Map<Integer, Integer> spawnPoints = new HashMap<>();
        spawnPoints.put(1, 0);
        int spawnPointIdx = 1;
        for (int p : players.keySet()) {
            spawnPoints.put(p, spawnPointIdx);
            spawnPointIdx++;
        }

        for (Map.Entry<Integer, Integer> entry : spawnPoints.entrySet()) {
            int pId = entry.getKey();
            int spawnIdx = entry.getValue();
            Godot spawnPos = (Godot) world.call("get_node", "SpawnPoints/" + spawnIdx);
            Vector2 pos = (Vector2) spawnPos.getProperty("position");
            Godot player = (Godot) playerScene.call("instantiate");
            player.setProperty("synced_position", pos);
            player.setProperty("name", String.valueOf(pId));
            Godot playersNode = (Godot) world.call("get_node", "Players");
            playersNode.call("add_child", player, false, 0);
            long uniqueId = (long) call("multiplayer.get_unique_id");
            String pName = pId == (int) uniqueId ? playerName : players.get(pId);
            player.call("rpc", "set_player_name", pName);
        }
    }

    @GodotMethod
    public void end_game() {
        if ((boolean) call("has_node", "/root/World")) {
            ((Godot) call("get_node", "/root/World")).call("queue_free");
        }
        call("emit_signal", "game_ended");
        players.clear();
    }

    @GodotMethod
    public Object get_player_color(String pName) {
        int hash = pName.hashCode();
        double hue = wrapF(hash * 0.001, 0.0, 1.0);
        return call("Color.from_hsv", hue, 0.6, 1.0);
    }

    private double wrapF(double value, double min, double max) {
        double range = max - min;
        value = value - range * Math.floor((value - min) / range);
        return value;
    }
}
