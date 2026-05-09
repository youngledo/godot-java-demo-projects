package demos.threed.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.AnimationPlayer;
import org.godot.node.Area3D;

@GodotClass(name = "PLCoin", parent = "Area3D")
public class PLCoin extends Area3D {

    private boolean taken = false;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
    }

    @GodotMethod
    public void OnCoinBodyEnter(Object body) {
        if (taken || !(body instanceof PLPlayer player)) return;

        taken = true;
        AnimationPlayer anim = getNodeAs("Animation", AnimationPlayer.class);
        if (anim != null) anim.play("take");

        player.collectCoin();
    }
}
