package demos.networking.webrtc_minimal;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.node.Node;

@GodotClass(name = "WebRTCMinimalMain", parent = "Node")
public class WebRTCMinimalMain extends Node {

    @Override
    public void _ready() {
        Godot chatScene = (Godot) call("load", "res://chat.tscn");
        if (chatScene == null) return;
        Godot p1 = (Godot) chatScene.call("instantiate");
        Godot p2 = (Godot) chatScene.call("instantiate");
        if (p1 != null) call("add_child", p1, false, 0);
        if (p2 != null) call("add_child", p2, false, 0);

        // Wait one second and send message from P1.
        Godot tree = (Godot) getTree();
        Godot timer1 = (Godot) tree.call("create_timer", 1.0);
        timer1.connect("timeout", new org.godot.core.Callable(this, "_send_p1"), 0);

        // Wait two seconds and send message from P2.
        Godot timer2 = (Godot) tree.call("create_timer", 2.0);
        timer2.connect("timeout", new org.godot.core.Callable(this, "_send_p2"), 0);
    }

    @org.godot.annotation.GodotMethod
    public void SendP1() {
        Godot p1 = (Godot) getChild(0);
        if (p1 != null) {
            String path = String.valueOf(p1.call("get_path"));
            p1.call("send_message", "Hi from " + path);
        }
    }

    @org.godot.annotation.GodotMethod
    public void SendP2() {
        Godot p2 = (Godot) getChild(1);
        if (p2 != null) {
            String path = String.valueOf(p2.call("get_path"));
            p2.call("send_message", "Hi from " + path);
        }
    }
}
