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
    public void lobbyJoined() {}

    @Signal
    public void lobbySealed() {}

    @Signal
    public void peerConnected() {}

    @Signal
    public void peerDisconnected() {}

    @Signal
    public void offerReceived() {}

    @Signal
    public void answerReceived() {}

    @Signal
    public void candidateReceived() {}

    @Override
    public void _ready() {
        super._ready();
        rtcMp = (Godot) call("WebRTCMultiplayerPeer.new");

		connect("connected", new Callable(this, "_connected"), 0);
		connect("disconnected", new Callable(this, "_disconnected"), 0);
		connect("offer_received", new Callable(this, "_offer_received"), 0);
		connect("answer_received", new Callable(this, "_answer_received"), 0);
		connect("candidate_received", new Callable(this, "_candidate_received"), 0);
		connect("lobby_joined", new Callable(this, "_lobby_joined"), 0);
		connect("lobby_sealed", new Callable(this, "_lobby_sealed"), 0);
		connect("peer_connected", new Callable(this, "_peer_connected"), 0);
		connect("peer_disconnected", new Callable(this, "_peer_disconnected"), 0);
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
        Godot mp = (Godot) getMultiplayer();
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
        peer.connect("session_description_created", new Callable(this, "_offer_created", id), 0);
        peer.connect("ice_candidate_created", new Callable(this, "_new_ice_candidate", id), 0);
        rtcMp.call("add_peer", peer, id);
        long uniqueId = (long) rtcMp.call("get_unique_id");
        if (id < uniqueId) {
            peer.call("create_offer");
        }
        return peer;
    }

    @GodotMethod
    public void NewIceCandidate(String midName, int indexName, String sdpName, int id) {
        sendCandidate(id, midName, indexName, sdpName);
    }

    @GodotMethod
    public void OfferCreated(String type, String data, int id) {
        if (!(boolean) rtcMp.call("has_peer", id)) return;
        System.out.println("created " + type);
        Godot peerObj = (Godot) rtcMp.call("get_peer", id);
        Godot connection = (Godot) peerObj.getProperty("connection");
        connection.call("set_local_description", type, data);
        if ("offer".equals(type)) {
            sendOffer(id, data);
        } else {
            sendAnswer(id, data);
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
        Godot mp = (Godot) getMultiplayer();
        mp.setProperty("multiplayer_peer", rtcMp);
    }

    @GodotMethod
    public void LobbyJoined(String lobbyStr) {
        lobby = lobbyStr;
    }

    @GodotMethod
    public void LobbySealed() {
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
    public void PeerConnected(int id) {
        System.out.println("Peer connected: " + id);
        createPeer(id);
    }

    @GodotMethod
    public void PeerDisconnected(int id) {
        if ((boolean) rtcMp.call("has_peer", id)) {
            rtcMp.call("remove_peer", id);
        }
    }

    @GodotMethod
    public void OfferReceived(int id, String offer) {
        System.out.println("Got offer: " + id);
        if ((boolean) rtcMp.call("has_peer", id)) {
            Godot peerObj = (Godot) rtcMp.call("get_peer", id);
            Godot connection = (Godot) peerObj.getProperty("connection");
            connection.call("set_remote_description", "offer", offer);
        }
    }

    @GodotMethod
    public void AnswerReceived(int id, String answer) {
        System.out.println("Got answer: " + id);
        if ((boolean) rtcMp.call("has_peer", id)) {
            Godot peerObj = (Godot) rtcMp.call("get_peer", id);
            Godot connection = (Godot) peerObj.getProperty("connection");
            connection.call("set_remote_description", "answer", answer);
        }
    }

    @GodotMethod
    public void CandidateReceived(int id, String mid, int index, String sdp) {
        if ((boolean) rtcMp.call("has_peer", id)) {
            Godot peerObj = (Godot) rtcMp.call("get_peer", id);
            Godot connection = (Godot) peerObj.getProperty("connection");
            connection.call("add_ice_candidate", mid, index, sdp);
        }
    }
}
