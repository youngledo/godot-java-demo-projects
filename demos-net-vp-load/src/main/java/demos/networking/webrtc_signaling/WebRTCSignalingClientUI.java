package demos.networking.webrtc_signaling;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.Control;

@GodotClass(name = "WebRTCSignalingClientUI", parent = "Control")
public class WebRTCSignalingClientUI extends Control {

    private Godot client;
    private Godot host;
    private Godot room;
    private Godot meshCheckBox;

    @Override
    public void _ready() {
        client = (Godot) getNode("Client");
        host = (Godot) getNode("VBoxContainer/Connect/Host");
        room = (Godot) getNode("VBoxContainer/Connect/RoomSecret");
        meshCheckBox = (Godot) getNode("VBoxContainer/Connect/Mesh");

        client.call("connect", "lobby_joined", new Callable(this, "_lobby_joined"), 0);
        client.call("connect", "lobby_sealed", new Callable(this, "_lobby_sealed"), 0);
        client.call("connect", "connected", new Callable(this, "_connected"), 0);
        client.call("connect", "disconnected", new Callable(this, "_disconnected"), 0);

        Godot mp = (Godot) getMultiplayer();
        mp.call("connect", "connected_to_server", new Callable(this, "_mp_server_connected"), 0);
        mp.call("connect", "connection_failed", new Callable(this, "_mp_server_disconnect"), 0);
        mp.call("connect", "server_disconnected", new Callable(this, "_mp_server_disconnect"), 0);
        mp.call("connect", "peer_connected", new Callable(this, "_mp_peer_connected"), 0);
        mp.call("connect", "peer_disconnected", new Callable(this, "_mp_peer_disconnected"), 0);
    }

    @GodotMethod
    public void ping(double argument) {
        long senderId = (long) call("multiplayer.get_remote_sender_id");
        logMsg("[Multiplayer] Ping from peer " + senderId + ": arg: " + argument);
    }

    @GodotMethod
    public void MpServerConnected() {
        Godot rtcMp = (Godot) client.getProperty("rtc_mp");
        logMsg("[Multiplayer] Server connected (I am " + rtcMp.call("get_unique_id") + ")");
    }

    @GodotMethod
    public void MpServerDisconnect() {
        Godot rtcMp = (Godot) client.getProperty("rtc_mp");
        logMsg("[Multiplayer] Server disconnected (I am " + rtcMp.call("get_unique_id") + ")");
    }

    @GodotMethod
    public void MpPeerConnected(int id) {
        logMsg("[Multiplayer] Peer " + id + " connected");
    }

    @GodotMethod
    public void MpPeerDisconnected(int id) {
        logMsg("[Multiplayer] Peer " + id + " disconnected");
    }

    @GodotMethod
    public void _connected(int id, boolean useMesh) {
        logMsg("[Signaling] Server connected with ID: " + id + ". Mesh: " + useMesh);
    }

    @GodotMethod
    public void _disconnected() {
        logMsg("[Signaling] Server disconnected: " + client.getProperty("code") + " - " + client.getProperty("reason"));
    }

    @GodotMethod
    public void LobbyJoined(String lobbyStr) {
        logMsg("[Signaling] Joined lobby " + lobbyStr);
    }

    @GodotMethod
    public void LobbySealed() {
        logMsg("[Signaling] Lobby has been sealed");
    }

    private void logMsg(String msg) {
        System.out.println(msg);
        Godot textEdit = (Godot) getNode("VBoxContainer/TextEdit");
        textEdit.setProperty("text", textEdit.getProperty("text") + msg + "\n");
    }

    @GodotMethod
    public void OnPeersPressed() {
        Godot mp = (Godot) getMultiplayer();
        logMsg(String.valueOf(mp.call("get_peers")));
    }

    @GodotMethod
    public void OnPingPressed() {
        double randVal = Math.random();
        call("rpc", "ping", randVal);
    }

    @GodotMethod
    public void OnSealPressed() {
        client.call("seal_lobby");
    }

    @GodotMethod
    public void OnStartPressed() {
        String hostText = (String) host.getProperty("text");
        String roomText = (String) room.getProperty("text");
        boolean meshVal = (boolean) meshCheckBox.getProperty("button_pressed");
        client.call("start", hostText, roomText, meshVal);
    }

    @GodotMethod
    public void OnStopPressed() {
        client.call("stop");
    }
}
