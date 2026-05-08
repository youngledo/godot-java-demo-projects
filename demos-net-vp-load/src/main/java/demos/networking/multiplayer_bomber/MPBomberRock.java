package demos.networking.multiplayer_bomber;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.AnimationPlayer;
import org.godot.node.CharacterBody2D;

@GodotClass(name = "MPBomberRock", parent = "CharacterBody2D")
public class MPBomberRock extends CharacterBody2D {

    @GodotMethod
    public void exploded(int byWho) {
        MPBomberScore score = getNodeAs("../../Score", MPBomberScore.class);
        score.increaseScore(byWho);
        AnimationPlayer animPlayer = getNodeAs("AnimationPlayer", AnimationPlayer.class);
        animPlayer.play("explode");
    }
}
