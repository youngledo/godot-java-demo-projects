package demos.networking.multiplayer_bomber;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.MultiplayerSpawner;

@GodotClass(name = "MPBomberBombSpawner", parent = "MultiplayerSpawner")
public class MPBomberBombSpawner extends MultiplayerSpawner {

    @Override
    public void _ready() {
        // Set the spawn function
        call("set_spawn_function", this, "_spawn_bomb");
    }

    @GodotMethod
    public Object _spawn_bomb(Object[] data) {
        if (data == null || data.length != 2) return null;

        // Validate data types - check for Vector2 and integer
        Object posData = data[0];
        Object idData = data[1];
        if (posData == null || idData == null) return null;
        if (!(idData instanceof Number)) return null;

        Godot scene = (Godot) call("load", "res://bomb.tscn");
        Godot bomb = (Godot) scene.call("instantiate");
        bomb.setProperty("position", data[0]);
        bomb.setProperty("from_player", ((Number) data[1]).intValue());
        return bomb;
    }
}
