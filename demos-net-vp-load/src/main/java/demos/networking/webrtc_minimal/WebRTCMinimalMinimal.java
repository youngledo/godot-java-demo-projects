package demos.networking.webrtc_minimal;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.core.Callable;
import org.godot.node.Node;

@GodotClass(name = "WebRTCMinimalMinimal", parent = "Node")
public class WebRTCMinimalMinimal extends Node {

    private Godot p1;
    private Godot p2;
    private Godot ch1;
    private Godot ch2;

    @Override
    public void _ready() {
        p1 = (Godot) call("WebRTCPeerConnection.new");
        p2 = (Godot) call("WebRTCPeerConnection.new");

        // Create negotiated data channels with id=1
        ch1 = (Godot) p1.call("create_data_channel", "chat", java.util.Map.of("id", 1, "negotiated", true));
        ch2 = (Godot) p2.call("create_data_channel", "chat", java.util.Map.of("id", 1, "negotiated", true));

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
        p1.call("create_offer");
    }

    @Override
    public void _process(double delta) {
        p1.call("poll");
        p2.call("poll");

        long ch1State = (long) ch1.call("get_ready_state");
        if (ch1State == 1L && (long) ch1.call("get_available_packet_count") > 0) { // STATE_OPEN
            byte[] pkt = (byte[]) ch1.call("get_packet");
            System.out.println("P1 received: " + new String(pkt));
        }

        long ch2State = (long) ch2.call("get_ready_state");
        if (ch2State == 1L && (long) ch2.call("get_available_packet_count") > 0) { // STATE_OPEN
            byte[] pkt = (byte[]) ch2.call("get_packet");
            System.out.println("P2 received: " + new String(pkt));
        }
    }
}
