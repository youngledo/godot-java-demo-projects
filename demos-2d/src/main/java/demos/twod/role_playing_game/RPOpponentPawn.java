package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;

@GodotClass(name = "RPOpponentPawn", parent = "Node2D")
public class RPOpponentPawn extends RPWalker {

    @Override
    public void _ready() {
        super._ready();
        call("set_process", false);
    }
}
