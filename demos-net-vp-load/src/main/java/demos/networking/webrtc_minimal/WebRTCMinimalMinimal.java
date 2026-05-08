package demos.networking.webrtc_minimal;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.collection.GodotDictionary;
import org.godot.core.Callable;
import org.godot.node.Node;
import org.godot.node.WebRTCPeerConnection;
import org.godot.node.WebRTCDataChannel;

@GodotClass(name = "WebRTCMinimalMinimal", parent = "Node")
public class WebRTCMinimalMinimal extends Node {

    private WebRTCPeerConnection p1;
    private WebRTCPeerConnection p2;
    private WebRTCDataChannel ch1;
    private WebRTCDataChannel ch2;

    @Override
    public void _ready() {
        p1 = WebRTCPeerConnection.create();
        p2 = WebRTCPeerConnection.create();

        // Create negotiated data channels with id=1
        GodotDictionary opts = new GodotDictionary();
        opts.put("id", 1);
        opts.put("negotiated", true);
        ch1 = p1.createDataChannel("chat", opts);
        ch2 = p2.createDataChannel("chat", opts);

        // Connect P1 session created to itself to set local description.
        p1.connect("session_description_created", new org.godot.core.Callable(p1, "set_local_description"), 0);
        // Connect P1 session and ICE created to p2 set remote description and candidates.
        p1.connect("session_description_created", new org.godot.core.Callable(p2, "set_remote_description"), 0);
        p1.connect("ice_candidate_created", new org.godot.core.Callable(p2, "add_ice_candidate"), 0);

        // Same for P2.
        p2.connect("session_description_created", new org.godot.core.Callable(p2, "set_local_description"), 0);
        p2.connect("session_description_created", new org.godot.core.Callable(p1, "set_remote_description"), 0);
        p2.connect("ice_candidate_created", new org.godot.core.Callable(p1, "add_ice_candidate"), 0);

        // Let P1 create the offer.
        p1.createOffer();
    }

    @Override
    public void _process(double delta) {
        p1.poll();
        p2.poll();

        int ch1State = ch1.getReadyState();
        if (ch1State == 1L && ch1.getAvailablePacketCount() > 0) { // STATE_OPEN
            byte[] pkt = ch1.getPacket();
            System.out.println("P1 received: " + new String(pkt));
        }

        int ch2State = ch2.getReadyState();
        if (ch2State == 1L && ch2.getAvailablePacketCount() > 0) { // STATE_OPEN
            byte[] pkt = ch2.getPacket();
            System.out.println("P2 received: " + new String(pkt));
        }
    }
}
