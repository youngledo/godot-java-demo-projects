package demos.networking.webrtc_signaling;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.collection.GodotDictionary;
import org.godot.core.Callable;
import org.godot.node.Node;
import org.godot.node.WebRTCMultiplayerPeer;
import org.godot.node.WebRTCPeerConnection;

@GodotClass(name = "WebRTCSignalingMultiplayerClient", parent = "Node")
public class WebRTCSignalingMultiplayerClient extends WebRTCSignalingWSClient {

    private WebRTCMultiplayerPeer rtcMp;
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
        rtcMp = WebRTCMultiplayerPeer.create();

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
        rtcMp.close();
        close();
    }

    private WebRTCPeerConnection createPeer(int id) {
        WebRTCPeerConnection peer = WebRTCPeerConnection.create();
        GodotDictionary config = new GodotDictionary();
        GodotDictionary iceServer = new GodotDictionary();
        iceServer.put("urls", new String[]{"stun:stun.l.google.com:19302"});
        config.put("iceServers", new GodotDictionary[]{iceServer});
        peer.initialize(config);
        peer.connect("session_description_created", new Callable(this, "_offer_created", id), 0);
        peer.connect("ice_candidate_created", new Callable(this, "_new_ice_candidate", id), 0);
        rtcMp.addPeer(peer, id);
        int uniqueId = rtcMp.getUniqueId();
        if (id < uniqueId) {
            peer.createOffer();
        }
        return peer;
    }

    @GodotMethod
    public void NewIceCandidate(String midName, int indexName, String sdpName, int id) {
        sendCandidate(id, midName, indexName, sdpName);
    }

    @GodotMethod
    public void OfferCreated(String type, String data, int id) {
        if (!rtcMp.hasPeer(id)) return;
        System.out.println("created " + type);
        GodotDictionary peerObj = rtcMp.getPeer(id);
        WebRTCPeerConnection connection = (WebRTCPeerConnection) peerObj.get("connection");
        connection.setLocalDescription(type, data);
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
            rtcMp.createMesh(id);
        } else if (id == 1) {
            rtcMp.createServer();
        } else {
            rtcMp.createClient(id);
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
        if (rtcMp.hasPeer(id)) {
            rtcMp.removePeer(id);
        }
    }

    @GodotMethod
    public void OfferReceived(int id, String offer) {
        System.out.println("Got offer: " + id);
        if (rtcMp.hasPeer(id)) {
            GodotDictionary peerObj = rtcMp.getPeer(id);
            WebRTCPeerConnection connection = (WebRTCPeerConnection) peerObj.get("connection");
            connection.setRemoteDescription("offer", offer);
        }
    }

    @GodotMethod
    public void AnswerReceived(int id, String answer) {
        System.out.println("Got answer: " + id);
        if (rtcMp.hasPeer(id)) {
            GodotDictionary peerObj = rtcMp.getPeer(id);
            WebRTCPeerConnection connection = (WebRTCPeerConnection) peerObj.get("connection");
            connection.setRemoteDescription("answer", answer);
        }
    }

    @GodotMethod
    public void CandidateReceived(int id, String mid, int index, String sdp) {
        if (rtcMp.hasPeer(id)) {
            GodotDictionary peerObj = rtcMp.getPeer(id);
            WebRTCPeerConnection connection = (WebRTCPeerConnection) peerObj.get("connection");
            connection.addIceCandidate(mid, index, sdp);
        }
    }
}
