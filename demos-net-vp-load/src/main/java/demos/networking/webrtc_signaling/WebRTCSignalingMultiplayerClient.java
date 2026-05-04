package demos.networking.webrtc_signaling;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.core.Callable;
import org.godot.node.Node;

@GodotClass(name = "WebRTCSignalingMultiplayerClient", parent = "Node")
public class WebRTCSignalingMultiplayerClient extends WebRTCSignalingWSClient {

    private Godot rtcMp;
    private boolean sealed = false;

    // Re-declare signals from parent class to ensure they are registered on this Godot class
    @Signal
    public void connected() {}

    @Signal
    public void disconnected() {}

    @Signal
    public void lobby_joined() {}

    @Signal
    public void lobby_sealed() {}

    @Signal
    public void peer_connected() {}

    @Signal
    public void peer_disconnected() {}

    @Signal
    public void offer_received() {}

    @Signal
    public void answer_received() {}

    @Signal
    public void candidate_received() {}

    @Override
    public void _ready() {
        super._ready();
        rtcMp = (Godot) call("WebRTCMultiplayerPeer.new");

        call("connect", "connected", this, "_connected");
        call("connect", "disconnected", this, "_disconnected");
        call("connect", "offer_received", this, "_offer_received");
        call("connect", "answer_received", this, "_answer_received");
        call("connect", "candidate_received", this, "_candidate_received");
        call("connect", "lobby_joined", this, "_lobby_joined");
        call("connect", "lobby_sealed", this, "_lobby_sealed");
        call("connect", "peer_connected", this, "_peer_connected");
        call("connect", "peer_disconnected", this, "_peer_disconnected");
    }

    @GodotMethod
    public void start(String url, String lobbyStr, boolean meshVal) {
        stop();
        sealed = false;
        mesh = meshVal;
        lobby = lobbyStr;
        connectToUrl(url);
    }

    @GodotMethod
    public void stop() {
        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", null);
        rtcMp.call("close");
        close();
    }

    private Godot createPeer(int id) {
        Godot peer = (Godot) call("WebRTCPeerConnection.new");
        java.util.Map<String, Object> config = java.util.Map.of(
            "iceServers", new Object[]{java.util.Map.of("urls", new String[]{"stun:stun.l.google.com:19302"})}
        );
        peer.call("initialize", config);
        peer.call("connect", "session_description_created", new Callable(this, "_offer_created", id), 0);
        peer.call("connect", "ice_candidate_created", new Callable(this, "_new_ice_candidate", id), 0);
        rtcMp.call("add_peer", peer, id);
        long uniqueId = (long) rtcMp.call("get_unique_id");
        if (id < uniqueId) {
            peer.call("create_offer");
        }
        return peer;
    }

    @GodotMethod
    public void _new_ice_candidate(String midName, int indexName, String sdpName, int id) {
        send_candidate(id, midName, indexName, sdpName);
    }

    @GodotMethod
    public void _offer_created(String type, String data, int id) {
        if (!(boolean) rtcMp.call("has_peer", id)) return;
        System.out.println("created " + type);
        Godot peerObj = (Godot) rtcMp.call("get_peer", id);
        Godot connection = (Godot) peerObj.call("get", "connection");
        connection.call("set_local_description", type, data);
        if ("offer".equals(type)) {
            send_offer(id, data);
        } else {
            send_answer(id, data);
        }
    }

    @GodotMethod
    public void _connected(int id, boolean useMesh) {
        System.out.println("Connected " + id + ", mesh: " + useMesh);
        if (useMesh) {
            rtcMp.call("create_mesh", id);
        } else if (id == 1) {
            rtcMp.call("create_server");
        } else {
            rtcMp.call("create_client", id);
        }
        Godot mp = (Godot) call("get_multiplayer");
        mp.setProperty("multiplayer_peer", rtcMp);
    }

    @GodotMethod
    public void _lobby_joined(String lobbyStr) {
        lobby = lobbyStr;
    }

    @GodotMethod
    public void _lobby_sealed() {
        sealed = true;
    }

    @GodotMethod
    public void _disconnected() {
        System.out.println("Disconnected: " + code + ": " + reason);
        if (!sealed) {
            stop();
        }
    }

    @GodotMethod
    public void _peer_connected(int id) {
        System.out.println("Peer connected: " + id);
        createPeer(id);
    }

    @GodotMethod
    public void _peer_disconnected(int id) {
        if ((boolean) rtcMp.call("has_peer", id)) {
            rtcMp.call("remove_peer", id);
        }
    }

    @GodotMethod
    public void _offer_received(int id, String offer) {
        System.out.println("Got offer: " + id);
        if ((boolean) rtcMp.call("has_peer", id)) {
            Godot peerObj = (Godot) rtcMp.call("get_peer", id);
            Godot connection = (Godot) peerObj.call("get", "connection");
            connection.call("set_remote_description", "offer", offer);
        }
    }

    @GodotMethod
    public void _answer_received(int id, String answer) {
        System.out.println("Got answer: " + id);
        if ((boolean) rtcMp.call("has_peer", id)) {
            Godot peerObj = (Godot) rtcMp.call("get_peer", id);
            Godot connection = (Godot) peerObj.call("get", "connection");
            connection.call("set_remote_description", "answer", answer);
        }
    }

    @GodotMethod
    public void _candidate_received(int id, String mid, int index, String sdp) {
        if ((boolean) rtcMp.call("has_peer", id)) {
            Godot peerObj = (Godot) rtcMp.call("get_peer", id);
            Godot connection = (Godot) peerObj.call("get", "connection");
            connection.call("add_ice_candidate", mid, index, sdp);
        }
    }
}
