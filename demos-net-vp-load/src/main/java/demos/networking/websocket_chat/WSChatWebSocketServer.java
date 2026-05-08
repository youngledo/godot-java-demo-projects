package demos.networking.websocket_chat;

import org.godot.Godot;
import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.Signal;
import org.godot.node.Node;
import org.godot.node.TCPServer;
import org.godot.node.WebSocketPeer;
import org.godot.node.StreamPeerTCP;
import org.godot.node.StreamPeerSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GodotClass(name = "WSChatWebSocketServer", parent = "Node")
public class WSChatWebSocketServer extends Node {

    @Export
    public String[] handshakeHeaders = new String[0];

    @Export
    public String[] supportedProtocols = new String[0];

    @Export
    public int handshakeTimeout = 3000;

    @Export
    public boolean useRefuseNewConnections = false;

    @Signal
    public void clientConnected() {}

    @Signal
    public void clientDisconnected() {}

    @Signal
    public void messageReceived() {}

    private TCPServer tcpServer;
    private List<PendingPeer> pendingPeers = new ArrayList<>();
    private Map<Integer, WebSocketPeer> peers = new HashMap<>();

    private static class PendingPeer {
        long connectTime;
        StreamPeerTCP tcp;
        StreamPeerTCP connection;
        WebSocketPeer ws;

        PendingPeer(StreamPeerTCP tcp) {
            this.tcp = tcp;
            this.connection = tcp;
            this.connectTime = System.currentTimeMillis();
        }
    }

    @Override
    public void _ready() {
        tcpServer = TCPServer.create();
    }

    public long listen(int port) {
        return tcpServer.listen(port);
    }

    public void stop() {
        tcpServer.stop();
        pendingPeers.clear();
        peers.clear();
    }

    public WebSocketPeer getPeer(int peerId) {
        return peers.get(peerId);
    }

    public int send(int peerId, String message) {
        if (peerId <= 0) {
            for (Map.Entry<Integer, WebSocketPeer> entry : peers.entrySet() ) {
                if (entry.getKey() == -peerId) continue;
                entry.getValue().sendText(message);
            }
            return 0; // OK
        }

        WebSocketPeer socket = peers.get(peerId);
        if (socket == null) return -1;
        return socket.sendText(message);
    }

    private WebSocketPeer createPeer() {
        WebSocketPeer ws = WebSocketPeer.create();
        ws.setSupportedProtocols(supportedProtocols);
        ws.setHandshakeHeaders(handshakeHeaders);
        return ws;
    }

    public void poll() {
        if (!tcpServer.isListening()) return;

        while (!useRefuseNewConnections && tcpServer.isConnectionAvailable()) {
            StreamPeerTCP conn = tcpServer.takeConnection();
            pendingPeers.add(new PendingPeer(conn));
        }

        List<PendingPeer> toRemove = new ArrayList<>();
        for (PendingPeer p : pendingPeers) {
            if (!connectPending(p)) {
                if (p.connectTime + handshakeTimeout < System.currentTimeMillis() ) {
                    toRemove.add(p);
                }
                continue;
            }
            toRemove.add(p);
        }
        pendingPeers.removeAll(toRemove);
        toRemove.clear();

        List<Integer> peersToRemove = new ArrayList<>();
        for (Map.Entry<Integer, WebSocketPeer> entry : peers.entrySet() ) {
            WebSocketPeer p = entry.getValue();
            p.poll();

            int state = p.getReadyState();
            if (state != 1L) { // Not STATE_OPEN
                emitSignal("client_disconnected", entry.getKey());
                peersToRemove.add(entry.getKey());
                continue;
            }

            while (p.getAvailablePacketCount() > 0) {
                byte[] pkt = p.getPacket();
                String msg;
                if (p.wasStringPacket()) {
                    msg = new String(pkt);
                } else {
                    msg = (String) call("bytes_to_var", pkt);
                }
                emitSignal("message_received", entry.getKey(), msg);
            }
        }
        for (Integer id : peersToRemove) {
            peers.remove(id);
        }
    }

    private boolean connectPending(PendingPeer p) {
        if (p.ws != null) {
            p.ws.poll();
            int state = p.ws.getReadyState();
            if (state == 1L) { // STATE_OPEN
                int id = (int) (Math.random() * (Integer.MAX_VALUE - 2)) + 2;
                peers.put(id, p.ws);
                emitSignal("client_connected", id);
                return true;
            } else if (state != 0L) { // Not STATE_CONNECTING
                return true;
            }
            return false;
        } else {
            int status = p.tcp.getStatus();
            if (status != 2L) { // Not STATUS_CONNECTED
                return true;
            }
            p.ws = createPeer();
            p.ws.acceptStream(p.tcp);
            return false;
        }
    }

    @Override
    public void _process(double delta) {
        poll();
    }
}
