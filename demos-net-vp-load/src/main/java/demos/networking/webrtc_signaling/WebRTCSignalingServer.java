package demos.networking.webrtc_signaling;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.collection.GodotDictionary;
import org.godot.node.JSON;
import org.godot.node.Node;
import org.godot.node.TCPServer;
import org.godot.node.WebSocketPeer;
import org.godot.node.StreamPeerTCP;

import java.util.*;

@GodotClass(name = "WebRTCSignalingServer", parent = "Node")
public class WebRTCSignalingServer extends Node {

    private static final int MSG_JOIN = 0;
    private static final int MSG_ID = 1;
    private static final int MSG_PEER_CONNECT = 2;
    private static final int MSG_PEER_DISCONNECT = 3;
    private static final int MSG_OFFER = 4;
    private static final int MSG_ANSWER = 5;
    private static final int MSG_CANDIDATE = 6;
    private static final int MSG_SEAL = 7;

    private static final long TIMEOUT = 1000;
    private static final long SEAL_TIME = 10000;
    private static final String ALFNUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private Random rand = new Random();
    private Map<String, Lobby> lobbies = new LinkedHashMap<>();
    private TCPServer tcpServer;
    private Map<Integer, Peer> peers = new LinkedHashMap<>();

    private class Peer {
        int id;
        String lobby = "";
        long time;
        WebSocketPeer ws;

        Peer(int peerId, StreamPeerTCP tcp) {
            id = peerId;
            ws = WebSocketPeer.create();
            ws.acceptStream(tcp);
            time = System.currentTimeMillis();
        }

        boolean isWsOpen() {
            return ws.getReadyState() == 1L;
        }

        void send(int type, int idVal, String data) {
            String json = "{\"type\":" + type + ",\"id\":" + idVal + ",\"data\":\"" + data.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
            ws.sendText(json);
        }
    }

    private class Lobby {
        Map<Integer, Peer> peers = new LinkedHashMap<>();
        int host;
        boolean sealed = false;
        long time = 0;
        boolean mesh;

        Lobby(int hostId, boolean useMesh) {
            host = hostId;
            mesh = useMesh;
        }

        boolean join(Peer peer) {
            if (sealed) return false;
            if (!peer.isWsOpen()) return false;
            peer.send(MSG_ID, peer.id == host ? 1 : peer.id, mesh ? "true" : "");
            for (Peer p : peers.values() ) {
                if (!p.isWsOpen()) continue;
                if (!mesh && p.id != host) continue;
                p.send(MSG_PEER_CONNECT, peer.id, "");
                peer.send(MSG_PEER_CONNECT, p.id == host ? 1 : p.id, "");
            }
            peers.put(peer.id, peer);
            return true;
        }

        boolean leave(Peer peer) {
            if (!peers.containsKey(peer.id)) return false;
            peers.remove(peer.id);
            boolean close = peer.id == host;
            if (sealed) return close;
            for (Peer p : peers.values() ) {
                if (!p.isWsOpen()) continue;
                if (close) {
                    p.ws.close();
                } else {
                    p.send(MSG_PEER_DISCONNECT, peer.id, "");
                }
            }
            return close;
        }

        boolean seal(int peerId) {
            if (host != peerId) return false;
            sealed = true;
            for (Peer p : peers.values() ) {
                if (!p.isWsOpen()) continue;
                p.send(MSG_SEAL, 0, "");
            }
            time = System.currentTimeMillis();
            peers.clear();
            return true;
        }
    }

    @Override
    public void _ready() {
        tcpServer = TCPServer.create();
    }

    @GodotMethod
    public void listen(int port) {
        stop();
        rand.setSeed(System.currentTimeMillis());
        tcpServer.listen(port);
    }

    @GodotMethod
    public void stop() {
        tcpServer.stop();
        peers.clear();
    }

    @Override
    public void _process(double delta) {
        poll();
    }

    private void poll() {
        if (!tcpServer.isListening()) return;

        if (tcpServer.isConnectionAvailable()) {
            int id = rand.nextInt(Integer.MAX_VALUE);
            StreamPeerTCP conn = tcpServer.takeConnection();
            peers.put(id, new Peer(id, conn));
        }

        List<Integer> toRemove = new ArrayList<>();
        for (Map.Entry<Integer, Peer> entry : peers.entrySet() ) {
            Peer p = entry.getValue();
            if (p.lobby.isEmpty() && System.currentTimeMillis() - p.time > TIMEOUT) {
                p.ws.close();
            }
            p.ws.poll();
            while (p.isWsOpen() && p.ws.getAvailablePacketCount() > 0) {
                if (!parseMsg(p)) {
                    System.out.println("Parse message failed from peer " + p.id);
                    toRemove.add(p.id);
                    p.ws.close();
                    break;
                }
            }
            int state = p.ws.getReadyState();
            if (state == 3L) {
                System.out.println("Peer " + p.id + " disconnected from lobby: '" + p.lobby + "'");
                if (lobbies.containsKey(p.lobby) && lobbies.get(p.lobby).leave(p)) {
                    System.out.println("Deleted lobby " + p.lobby);
                    lobbies.remove(p.lobby);
                }
                toRemove.add(p.id);
            }
        }

        for (String k : new ArrayList<>(lobbies.keySet()) ) {
            Lobby lobby = lobbies.get(k);
            if (!lobby.sealed) continue;
            if (lobby.time + SEAL_TIME < System.currentTimeMillis() ) {
                for (Integer pid : lobby.peers.keySet() ) {
                    Peer p = peers.get(pid);
                    if (p != null) {
                        p.ws.close();
                        toRemove.add(pid);
                    }
                }
            }
        }

        for (Integer id : toRemove) {
            peers.remove(id);
        }
    }

    private boolean joinLobby(Peer peer, String lobbyName, boolean meshVal) {
        if (lobbyName.isEmpty() ) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 32; i++) {
                sb.append(ALFNUM.charAt(rand.nextInt(ALFNUM.length())));
            }
            lobbyName = sb.toString();
            lobbies.put(lobbyName, new Lobby(peer.id, meshVal));
        } else if (!lobbies.containsKey(lobbyName)) {
            return false;
        }
        lobbies.get(lobbyName).join(peer);
        peer.lobby = lobbyName;
        peer.send(MSG_JOIN, 0, lobbyName);
        System.out.println("Peer " + peer.id + " joined lobby: '" + lobbyName + "'");
        return true;
    }

    private boolean parseMsg(Peer peer) {
        byte[] pkt = peer.ws.getPacket();
        String pktStr = new String(pkt);
        Object parsedObj = JSON.parseString(pktStr);
        if (!(parsedObj instanceof GodotDictionary)) return false;
        GodotDictionary parsed = (GodotDictionary) parsedObj;
        if (!parsed.containsKey("type")) return false;
        if (!parsed.containsKey("id")) return false;
        if (!parsed.containsKey("data")) return false;

        int msgType = ((Number) parsed.getProperty("type")).intValue();
        int msgId = ((Number) parsed.getProperty("id")).intValue();
        String msgData = (String) parsed.getProperty("data");

        if (msgType == MSG_JOIN) {
            if (!peer.lobby.isEmpty()) return false;
            return joinLobby(peer, msgData, msgId == 0);
        }

        if (!lobbies.containsKey(peer.lobby)) return false;
        Lobby lobby = lobbies.get(peer.lobby);

        if (msgType == MSG_SEAL) {
            return lobby.seal(peer.id);
        }

        int destId = msgId;
        if (destId == 1) destId = lobby.host;

        if (!peers.containsKey(destId)) return false;
        if (!peers.get(destId).lobby.equals(peer.lobby)) return false;

        if (msgType == MSG_OFFER || msgType == MSG_ANSWER || msgType == MSG_CANDIDATE) {
            int source = peer.id == lobby.host ? 1 : peer.id;
            peers.get(destId).send(msgType, source, msgData);
            return true;
        }

        return false;
    }
}
