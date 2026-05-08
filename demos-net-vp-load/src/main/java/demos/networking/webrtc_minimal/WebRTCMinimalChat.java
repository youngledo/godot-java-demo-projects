package demos.networking.webrtc_minimal;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.collection.GodotDictionary;
import org.godot.core.Callable;
import org.godot.node.Node;
import org.godot.node.WebRTCPeerConnection;
import org.godot.node.WebRTCDataChannel;

@GodotClass(name = "WebRTCMinimalChat", parent = "Node")
public class WebRTCMinimalChat extends Node {

    private WebRTCPeerConnection peer;
    private WebRTCDataChannel channel;

    @Override
    public void _ready() {
        peer = WebRTCPeerConnection.create();
        GodotDictionary opts = new GodotDictionary();
        opts.put("negotiated", true);
        opts.put("id", 1);
        channel = peer.createDataChannel("chat", opts);

        peer.connect("ice_candidate_created", new Callable(this, "_on_ice_candidate"), 0);
        peer.connect("session_description_created", new Callable(this, "_on_session"), 0);

        // Register to the local signaling server
        WebRTCMinimalSignaling signaling = getNodeAs("/root/Signaling", WebRTCMinimalSignaling.class);
        signaling.register(getPath().toString());
    }

    @GodotMethod
    public void OnIceCandidate(String media, int index, String sdp) {
        WebRTCMinimalSignaling signaling = getNodeAs("/root/Signaling", WebRTCMinimalSignaling.class);
        signaling.sendCandidate(getPath().toString(), media, index, sdp);
    }

    @GodotMethod
    public void OnSession(String type, String sdp) {
        WebRTCMinimalSignaling signaling = getNodeAs("/root/Signaling", WebRTCMinimalSignaling.class);
        signaling.sendSession(getPath().toString(), type, sdp);
        peer.setLocalDescription(type, sdp);
    }

    @Override
    public void _process(double delta) {
        peer.poll();
        int state = channel.getReadyState();
        if (state == 1L) { // STATE_OPEN
            while (channel.getAvailablePacketCount() > 0) {
                byte[] pkt = channel.getPacket();
                System.out.println(getPath().toString() + " received: " + new String(pkt));
            }
        }
    }

    @GodotMethod
    public void sendMessage(String message) {
        byte[] data = message.getBytes();
        channel.putPacket(data);
    }
}
