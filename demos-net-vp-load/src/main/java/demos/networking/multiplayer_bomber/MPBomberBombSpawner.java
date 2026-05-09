package demos.networking.multiplayer_bomber;

import org.godot.annotation.GodotClass;
import org.godot.core.Callable;
import org.godot.annotation.GodotMethod;
import org.godot.node.MultiplayerSpawner;
import org.godot.node.Node;
import org.godot.node.PackedScene;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "MPBomberBombSpawner", parent = "MultiplayerSpawner")
public class MPBomberBombSpawner extends MultiplayerSpawner {

    @Override
    public void _ready() {
        setSpawnFunction(new Callable(this, "_spawn_bomb"));
    }

    @GodotMethod
    public Object SpawnBomb(Object[] data) {
        if (data == null || data.length != 2) return null;

        // Validate data types - check for Vector2 and integer
        Object posData = data[0];
        Object idData = data[1];
        if (posData == null || idData == null) return null;
        if (!(idData instanceof Number)) return null;

        PackedScene scene = (PackedScene) ResourceLoader.singleton().load("res://bomb.tscn");
        Node bomb = scene.instantiate();
        bomb.setProperty("position", data[0]);
        bomb.setProperty("from_player", ((Number) data[1]).intValue());
        return bomb;
    }
}
