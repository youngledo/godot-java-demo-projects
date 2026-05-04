package demos.networking.websocket_chat;

import org.godot.Godot;
import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.Signal;
import org.godot.node.Node;

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
    public void client_connected() {}

    @Signal
    public void client_disconnected() {}

    @Signal
    public void message_received() {}

    private Godot tcpServer;
    private List<PendingPeer> pendingPeers = new ArrayList<>();
    private Map<Integer, Godot> peers = new HashMap<>();

    private static class PendingPeer {
        long connectTime;
        Godot tcp;
        Godot connection;
        Godot ws;

        PendingPeer(Godot tcp) {
            this.tcp = tcp;
            this.connection = tcp;
            this.connectTime = System.currentTimeMillis();
        }
    }

    @Override
    public void _ready() {
        tcpServer = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("TCPServer");
    }

    public long listen(int port) {
        return (long) tcpServer.call("listen", port);
    }

    public void stop() {
        tcpServer.call("stop");
        pendingPeers.clear();
        peers.clear();
    }

    public int send(int peerId, String message) {
        if (peerId <= 0) {
            for (Map.Entry<Integer, Godot> entry : peers.entrySet()) {
                if (entry.getKey() == -peerId) continue;
                entry.getValue().call("send_text", message);
            }
            return 0; // OK
        }

        Godot socket = peers.get(peerId);
        if (socket == null) return -1;
        return (int) socket.call("send_text", message);
    }

    private Godot createPeer() {
        Godot ws = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("WebSocketPeer");
        ws.setProperty("supported_protocols", supportedProtocols);
        ws.setProperty("handshake_headers", handshakeHeaders);
        return ws;
    }

    public void poll() {
        if (!(boolean) tcpServer.call("is_listening")) return;

        while (!useRefuseNewConnections && (boolean) tcpServer.call("is_connection_available")) {
            Godot conn = (Godot) tcpServer.call("take_connection");
            pendingPeers.add(new PendingPeer(conn));
        }

        List<PendingPeer> toRemove = new ArrayList<>();
        for (PendingPeer p : pendingPeers) {
            if (!connectPending(p)) {
                if (p.connectTime + handshakeTimeout < System.currentTimeMillis()) {
                    toRemove.add(p);
                }
                continue;
            }
            toRemove.add(p);
        }
        pendingPeers.removeAll(toRemove);
        toRemove.clear();

        List<Integer> peersToRemove = new ArrayList<>();
        for (Map.Entry<Integer, Godot> entry : peers.entrySet()) {
            Godot p = entry.getValue();
            p.call("poll");

            long state = (long) p.call("get_ready_state");
            if (state != 1L) { // Not STATE_OPEN
                call("emit_signal", "client_disconnected", entry.getKey());
                peersToRemove.add(entry.getKey());
                continue;
            }

            while ((long) p.call("get_available_packet_count") > 0) {
                Object pkt = p.call("get_packet");
                String msg;
                if ((boolean) p.call("was_string_packet")) {
                    msg = new String((byte[]) pkt);
                } else {
                    msg = (String) call("bytes_to_var", pkt);
                }
                call("emit_signal", "message_received", entry.getKey(), msg);
            }
        }
        for (Integer id : peersToRemove) {
            peers.remove(id);
        }
    }

    private boolean connectPending(PendingPeer p) {
        if (p.ws != null) {
            p.ws.call("poll");
            long state = (long) p.ws.call("get_ready_state");
            if (state == 1L) { // STATE_OPEN
                int id = (int) (Math.random() * (Integer.MAX_VALUE - 2)) + 2;
                peers.put(id, p.ws);
                call("emit_signal", "client_connected", id);
                return true;
            } else if (state != 0L) { // Not STATE_CONNECTING
                return true;
            }
            return false;
        } else {
            long status = (long) p.tcp.call("get_status");
            if (status != 2L) { // Not STATUS_CONNECTED
                return true;
            }
            p.ws = createPeer();
            p.ws.call("accept_stream", p.tcp);
            return false;
        }
    }

    @Override
    public void _process(double delta) {
        poll();
    }
}
