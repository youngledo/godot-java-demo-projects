package demos.networking.multiplayer_bomber;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.math.Vector2;
import org.godot.node.ENetMultiplayerPeer;
import org.godot.node.MultiplayerAPI;
import org.godot.node.Node;
import org.godot.node.PackedScene;
import org.godot.node.SceneTree;

import java.util.HashMap;
import java.util.Map;

@GodotClass(name = "MPBomberGameState", parent = "Node")
public class MPBomberGameState extends Node {

    private static final int DEFAULT_PORT = 10567;
    private static final int MAX_PEERS = 12;

    private ENetMultiplayerPeer peer;
    public String playerName = "The Warrior";
    public Map<Integer, String> players = new HashMap<>();

    @Signal
    public void playerListChanged() {}

    @Signal
    public void connectionSucceeded() {}

    @Signal
    public void connectionFailed() {}

    @Signal
    public void gameEnded() {}

    @Signal
    public void gameError() {}

    @Override
    public void _ready() {
        MultiplayerAPI mp = getMultiplayer();
        mp.connect("peer_connected", new org.godot.core.Callable(this, "_player_connected"), 0);
        mp.connect("peer_disconnected", new org.godot.core.Callable(this, "_player_disconnected"), 0);
        mp.connect("connected_to_server", new org.godot.core.Callable(this, "_connected_ok"), 0);
        mp.connect("connection_failed", new org.godot.core.Callable(this, "_connected_fail"), 0);
        mp.connect("server_disconnected", new org.godot.core.Callable(this, "_server_disconnected"), 0);
    }

    @GodotMethod
    public void PlayerConnected(long id) {
        call("rpc_id", (int) id, "register_player", playerName);
    }

    @GodotMethod
    public void PlayerDisconnected(long id) {
        if (hasNode("/root/World")) {
            if (getMultiplayer().isServer()) {
                emitSignal("game_error", "Player " + players.get((int) id) + " disconnected");
                endGame();
            }
        } else {
            unregisterPlayer((int) id);
        }
    }

    @GodotMethod
    public void ConnectedOk() {
        emitSignal("connection_succeeded");
    }

    @GodotMethod
    public void ServerDisconnected() {
        emitSignal("game_error", "Server disconnected");
        endGame();
    }

    @GodotMethod
    public void ConnectedFail() {
        getMultiplayer().setProperty("multiplayer_peer", null);
        emitSignal("connection_failed");
    }

    @GodotMethod
    public void registerPlayer(String newPlayerName) {
        int id = getMultiplayer().getRemoteSenderId();
        players.put(id, newPlayerName);
        emitSignal("player_list_changed");
    }

    private void unregisterPlayer(int id) {
        players.remove(id);
        emitSignal("player_list_changed");
    }

    @GodotMethod
    public void loadWorld() {
        Godot worldScene = (Godot) call("load", "res://world.tscn");
        Node world = ((PackedScene) worldScene).instantiate();
        SceneTree tree = getTree();
        Node root = tree.getRoot();
        root.addChild(world);
        root.getNode("Lobby").setProperty("visible", false);

        MPBomberScore score = world.getNodeAs("Score", MPBomberScore.class);
        int uniqueId = getMultiplayer().getUniqueId();
        score.addPlayer(uniqueId, playerName);
        for (int pn : players.keySet() ) {
            score.addPlayer(pn, players.get(pn));
        }

        tree.setProperty("paused", false);
    }

    @GodotMethod
    public void hostGame(String newPlayerName) {
        playerName = newPlayerName;
        peer = ENetMultiplayerPeer.create();
        peer.createServer(DEFAULT_PORT, MAX_PEERS);
        getMultiplayer().setProperty("multiplayer_peer", peer);
    }

    @GodotMethod
    public void joinGame(String ip, String newPlayerName) {
        playerName = newPlayerName;
        peer = ENetMultiplayerPeer.create();
        peer.createClient(ip, DEFAULT_PORT);
        getMultiplayer().setProperty("multiplayer_peer", peer);
    }

    @GodotMethod
    public Object getPlayerList() {
        return players.values().toArray();
    }

    @GodotMethod
    public void beginGame() {
        assert getMultiplayer().isServer();
        rpc("load_world");

        SceneTree tree = getTree();
        Node root = tree.getRoot();
        Node world = root.getNode("World");
        Godot playerScene = (Godot) call("load", "res://player.tscn");

        Map<Integer, Integer> spawnPoints = new HashMap<>();
        spawnPoints.put(1, 0);
        int spawnPointIdx = 1;
        for (int p : players.keySet() ) {
            spawnPoints.put(p, spawnPointIdx);
            spawnPointIdx++;
        }

        for (Map.Entry<Integer, Integer> entry : spawnPoints.entrySet() ) {
            int pId = entry.getKey();
            int spawnIdx = entry.getValue();
            Node spawnPos = world.getNode("SpawnPoints/" + spawnIdx);
            Vector2 pos = (Vector2) spawnPos.getProperty("position");
            Node player = ((PackedScene) playerScene).instantiate();
            player.setProperty("synced_position", pos);
            player.setProperty("name", String.valueOf(pId));
            Node playersNode = world.getNode("Players");
            playersNode.addChild(player);
            int uniqueId = getMultiplayer().getUniqueId();
            String pName = pId == uniqueId ? playerName : players.get(pId);
            player.call("rpc", "set_player_name", pName);
        }
    }

    @GodotMethod
    public void endGame() {
        if (hasNode("/root/World")) {
            getNode("/root/World").queueFree();
        }
        emitSignal("game_ended");
        players.clear();
    }

    @GodotMethod
    public Object getPlayerColor(String pName) {
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
