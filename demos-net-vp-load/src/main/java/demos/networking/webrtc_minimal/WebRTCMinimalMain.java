package demos.networking.webrtc_minimal;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.node.Node;
import org.godot.node.PackedScene;
import org.godot.node.SceneTree;
import org.godot.node.SceneTreeTimer;

@GodotClass(name = "WebRTCMinimalMain", parent = "Node")
public class WebRTCMinimalMain extends Node {

    @Override
    public void _ready() {
        Godot chatScene = (Godot) call("load", "res://chat.tscn");
        if (chatScene == null) return;
        PackedScene scene = (PackedScene) chatScene;
        Node p1 = scene.instantiate();
        Node p2 = scene.instantiate();
        if (p1 != null) addChild(p1);
        if (p2 != null) addChild(p2);

        SceneTree tree = getTree();
        SceneTreeTimer timer1 = tree.createTimer(1.0);
        timer1.connect("timeout", new org.godot.core.Callable(this, "_send_p1"), 0);

        SceneTreeTimer timer2 = tree.createTimer(2.0);
        timer2.connect("timeout", new org.godot.core.Callable(this, "_send_p2"), 0);
    }

    @org.godot.annotation.GodotMethod
    public void SendP1() {
        WebRTCMinimalChat p1 = (WebRTCMinimalChat) getChild(0);
        if (p1 != null) {
            String path = p1.getPath().toString();
            p1.sendMessage("Hi from " + path);
        }
    }

    @org.godot.annotation.GodotMethod
    public void SendP2() {
        WebRTCMinimalChat p2 = (WebRTCMinimalChat) getChild(1);
        if (p2 != null) {
            String path = p2.getPath().toString();
            p2.sendMessage("Hi from " + path);
        }
    }
}
