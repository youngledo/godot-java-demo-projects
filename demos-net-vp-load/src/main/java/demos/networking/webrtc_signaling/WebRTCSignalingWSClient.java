package demos.networking.webrtc_signaling;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.collection.GodotDictionary;
import org.godot.node.JSON;
import org.godot.node.Node;
import org.godot.node.WebSocketPeer;

@GodotClass(name = "WebRTCSignalingWSClient", parent = "Node")
public class WebRTCSignalingWSClient extends Node {

    // Message types matching the GDScript enum
    private static final int MSG_JOIN = 0;
    private static final int MSG_ID = 1;
    private static final int MSG_PEER_CONNECT = 2;
    private static final int MSG_PEER_DISCONNECT = 3;
    private static final int MSG_OFFER = 4;
    private static final int MSG_ANSWER = 5;
    private static final int MSG_CANDIDATE = 6;
    private static final int MSG_SEAL = 7;

    @Export
    public boolean autojoin = true;

    @Export
    public String lobby = "";

    @Export
    public boolean mesh = true;

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

    private WebSocketPeer ws;
    long code = 1000;
    String reason = "Unknown";
    private long oldState = 3L; // STATE_CLOSED

    @Override
    public void _ready() {
        ws = WebSocketPeer.create();
    }

    public void connectToUrl(String url) {
        close();
        code = 1000;
        reason = "Unknown";
        ws.connectToUrl(url);
    }

    public void close() {
        ws.close();
    }

    @Override
    public void _process(double delta) {
        ws.poll();
        int state = ws.getReadyState();

        if (state != oldState && state == 1L && autojoin) { // STATE_OPEN
            joinLobby(lobby);
        }

        while (state == 1L && ws.getAvailablePacketCount() > 0) {
            if (!parseMsg() ) {
                System.out.println("Error parsing message from server.");
            }
        }

        if (state != oldState && state == 3L) { // STATE_CLOSED
            code = ws.getCloseCode();
            reason = ws.getCloseReason();
            emitSignal("disconnected");
        }
        oldState = state;
    }

    private boolean parseMsg() {
        byte[] pkt = ws.getPacket();
        String pktStr = new String(pkt);
        Object parsedObj = JSON.parseString(pktStr);
        if (!(parsedObj instanceof GodotDictionary)) return false;
        GodotDictionary parsed = (GodotDictionary) parsedObj;

        long msgType = ((Number) parsed.get("type")).longValue();
        long srcId = ((Number) parsed.get("id")).longValue();
        String data = (String) parsed.get("data");

        if (data == null) return false;

        if (msgType == MSG_ID) {
            emitSignal("connected", (int) srcId, "true".equals(data));
        } else if (msgType == MSG_JOIN) {
            emitSignal("lobby_joined", data);
        } else if (msgType == MSG_SEAL) {
            emitSignal("lobby_sealed");
        } else if (msgType == MSG_PEER_CONNECT) {
            emitSignal("peer_connected", (int) srcId);
        } else if (msgType == MSG_PEER_DISCONNECT) {
            emitSignal("peer_disconnected", (int) srcId);
        } else if (msgType == MSG_OFFER) {
            emitSignal("offer_received", (int) srcId, data);
        } else if (msgType == MSG_ANSWER) {
            emitSignal("answer_received", (int) srcId, data);
        } else if (msgType == MSG_CANDIDATE) {
            String[] candidate = data.split("\n");
            if (candidate.length != 3) return false;
            try {
                int index = Integer.parseInt(candidate[1].trim());
                emitSignal("candidate_received", (int) srcId, candidate[0], index, candidate[2]);
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    @GodotMethod
    public long joinLobby(String lobbyName) {
        return sendMsg(MSG_JOIN, mesh ? 0 : 1, lobbyName);
    }

    @GodotMethod
    public long sealLobby() {
        return sendMsg(MSG_SEAL, 0, "");
    }

    @GodotMethod
    public long sendCandidate(int id, String mid, int index, String sdp) {
        return sendMsg(MSG_CANDIDATE, id, "\n" + mid + "\n" + index + "\n" + sdp);
    }

    @GodotMethod
    public long sendOffer(int id, String offer) {
        return sendMsg(MSG_OFFER, id, offer);
    }

    @GodotMethod
    public long sendAnswer(int id, String answer) {
        return sendMsg(MSG_ANSWER, id, answer);
    }

    private long sendMsg(int type, int id, String data) {
        GodotDictionary msg = new GodotDictionary();
        msg.put("type", type);
        msg.put("id", id);
        msg.put("data", data);
        String json = JSON.stringify(msg);
        return ws.sendText(json);
    }
}
