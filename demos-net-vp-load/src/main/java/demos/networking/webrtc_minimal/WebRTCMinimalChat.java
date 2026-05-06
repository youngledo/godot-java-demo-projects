package demos.networking.webrtc_minimal;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.Node;

@GodotClass(name = "WebRTCMinimalChat", parent = "Node")
public class WebRTCMinimalChat extends Node {

    private Godot peer;
    private Godot channel;

    @Override
    public void _ready() {
        peer = (Godot) call("WebRTCPeerConnection.new");
        channel = (Godot) peer.call("create_data_channel", "chat", java.util.Map.of("negotiated", true, "id", 1));

        peer.connect("ice_candidate_created", new Callable(this, "_on_ice_candidate"), 0);
        peer.connect("session_description_created", new Callable(this, "_on_session"), 0);

        // Register to the local signaling server
        Godot signaling = (Godot) getNode("/root/Signaling");
        signaling.call("register", String.valueOf(call("get_path")));
    }

    @GodotMethod
    public void OnIceCandidate(String media, int index, String sdp) {
        Godot signaling = (Godot) getNode("/root/Signaling");
        signaling.call("send_candidate", String.valueOf(call("get_path")), media, index, sdp);
    }

    @GodotMethod
    public void OnSession(String type, String sdp) {
        Godot signaling = (Godot) getNode("/root/Signaling");
        signaling.call("send_session", String.valueOf(call("get_path")), type, sdp);
        peer.call("set_local_description", type, sdp);
    }

    @Override
    public void _process(double delta) {
        peer.call("poll");
        long state = (long) channel.call("get_ready_state");
        if (state == 1L) { // STATE_OPEN
            while ((long) channel.call("get_available_packet_count") > 0) {
                byte[] pkt = (byte[]) channel.call("get_packet");
                System.out.println(String.valueOf(call("get_path")) + " received: " + new String(pkt));
            }
        }
    }

    @GodotMethod
    public void sendMessage(String message) {
        byte[] data = message.getBytes();
        channel.call("put_packet", data);
    }
}
