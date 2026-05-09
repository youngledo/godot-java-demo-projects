package demos.networking.webrtc_signaling;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.Control;
import org.godot.node.LineEdit;
import org.godot.node.CheckBox;
import org.godot.node.TextEdit;
import org.godot.node.MultiplayerAPI;
import org.godot.node.WebRTCMultiplayerPeer;

@GodotClass(name = "WebRTCSignalingClientUI", parent = "Control")
public class WebRTCSignalingClientUI extends Control {

    private WebRTCSignalingMultiplayerClient client;
    private LineEdit host;
    private LineEdit room;
    private CheckBox meshCheckBox;

    @Override
    public void _ready() {
        client = getNodeAs("Client", WebRTCSignalingMultiplayerClient.class);
        host = getNodeAs("VBoxContainer/Connect/Host", LineEdit.class);
        room = getNodeAs("VBoxContainer/Connect/RoomSecret", LineEdit.class);
        meshCheckBox = getNodeAs("VBoxContainer/Connect/Mesh", CheckBox.class);

        client.connect("lobby_joined", new Callable(this, "_lobby_joined"), 0);
        client.connect("lobby_sealed", new Callable(this, "_lobby_sealed"), 0);
        client.connect("connected", new Callable(this, "_connected"), 0);
        client.connect("disconnected", new Callable(this, "_disconnected"), 0);

        MultiplayerAPI mp = getMultiplayer();
        mp.connect("connected_to_server", new Callable(this, "_mp_server_connected"), 0);
        mp.connect("connection_failed", new Callable(this, "_mp_server_disconnect"), 0);
        mp.connect("server_disconnected", new Callable(this, "_mp_server_disconnect"), 0);
        mp.connect("peer_connected", new Callable(this, "_mp_peer_connected"), 0);
        mp.connect("peer_disconnected", new Callable(this, "_mp_peer_disconnected"), 0);
    }

    @GodotMethod
    public void ping(double argument) {
        int senderId = getMultiplayer().getRemoteSenderId();
        logMsg("[Multiplayer] Ping from peer " + senderId + ": arg: " + argument);
    }

    @GodotMethod
    public void MpServerConnected() {
        WebRTCMultiplayerPeer rtcMp = (WebRTCMultiplayerPeer) client.getProperty("rtc_mp");
        logMsg("[Multiplayer] Server connected (I am " + rtcMp.getUniqueId() + ")");
    }

    @GodotMethod
    public void MpServerDisconnect() {
        WebRTCMultiplayerPeer rtcMp = (WebRTCMultiplayerPeer) client.getProperty("rtc_mp");
        logMsg("[Multiplayer] Server disconnected (I am " + rtcMp.getUniqueId() + ")");
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
        TextEdit textEdit = getNodeAs("VBoxContainer/TextEdit", TextEdit.class);
        textEdit.setText(textEdit.getText() + msg + "\n");
    }

    @GodotMethod
    public void OnPeersPressed() {
        MultiplayerAPI mp = getMultiplayer();
        logMsg(String.valueOf(mp.getPeers()));
    }

    @GodotMethod
    public void OnPingPressed() {
        double randVal = Math.random();
        rpc("ping", randVal);
    }

    @GodotMethod
    public void OnSealPressed() {
        client.sealLobby();
    }

    @GodotMethod
    public void OnStartPressed() {
        String hostText = host.getText();
        String roomText = room.getText();
        boolean meshVal = meshCheckBox.isPressed();
        client.start(hostText, roomText, meshVal);
    }

    @GodotMethod
    public void OnStopPressed() {
        client.stop();
    }
}
