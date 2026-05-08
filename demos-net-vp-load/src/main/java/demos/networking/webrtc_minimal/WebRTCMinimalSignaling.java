package demos.networking.webrtc_minimal;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;
import org.godot.node.WebRTCPeerConnection;

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
            Godot node = (Godot) getNode(peers.get(0));
            WebRTCPeerConnection peerObj = (WebRTCPeerConnection) node.getProperty("peer");
            peerObj.createOffer();
        }
    }

    private String findOther(String path) {
        for (String p : peers) {
            if (!p.equals(path)) return p;
        }
        return "";
    }

    @GodotMethod
    public void sendSession(String path, String type, String sdp) {
        String other = findOther(path);
        assert !other.isEmpty();
        Godot node = (Godot) getNode(other);
        WebRTCPeerConnection peerObj = (WebRTCPeerConnection) node.getProperty("peer");
        peerObj.setRemoteDescription(type, sdp);
    }

    @GodotMethod
    public void sendCandidate(String path, String media, int index, String sdp) {
        String other = findOther(path);
        assert !other.isEmpty();
        Godot node = (Godot) getNode(other);
        WebRTCPeerConnection peerObj = (WebRTCPeerConnection) node.getProperty("peer");
        peerObj.addIceCandidate(media, index, sdp);
    }
}
