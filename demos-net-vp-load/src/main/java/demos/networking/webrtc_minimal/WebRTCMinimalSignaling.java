package demos.networking.webrtc_minimal;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;

import java.util.ArrayList;
import java.util.List;

@GodotClass(name = "WebRTCMinimalSignaling", parent = "Node")
public class WebRTCMinimalSignaling extends Node {

    private List<String> peers = new ArrayList<>();

    @GodotMethod
    public void register(String path) {
        assert peers.size() < 2;
        peers.add(path);
        if (peers.size() == 2) {
            Godot node = (Godot) call("get_node", peers.get(0));
            Godot peerObj = (Godot) node.getProperty("peer");
            peerObj.call("create_offer");
        }
    }

    private String findOther(String path) {
        for (String p : peers) {
            if (!p.equals(path)) return p;
        }
        return "";
    }

    @GodotMethod
    public void send_session(String path, String type, String sdp) {
        String other = findOther(path);
        assert !other.isEmpty();
        Godot node = (Godot) call("get_node", other);
        Godot peerObj = (Godot) node.getProperty("peer");
        peerObj.call("set_remote_description", type, sdp);
    }

    @GodotMethod
    public void send_candidate(String path, String media, int index, String sdp) {
        String other = findOther(path);
        assert !other.isEmpty();
        Godot node = (Godot) call("get_node", other);
        Godot peerObj = (Godot) node.getProperty("peer");
        peerObj.call("add_ice_candidate", media, index, sdp);
    }
}
