package demos.networking.multiplayer_bomber;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.CharacterBody2D;

@GodotClass(name = "MPBomberRock", parent = "CharacterBody2D")
public class MPBomberRock extends CharacterBody2D {

    @GodotMethod
    public void exploded(int byWho) {
        Godot score = (Godot) call("get_node", "../../Score");
        score.call("increase_score", byWho);
        Godot animPlayer = (Godot) call("get_node", "AnimationPlayer");
        animPlayer.call("play", "explode");
    }
}
