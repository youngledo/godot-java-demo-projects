package demos.networking.multiplayer_bomber;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Area2D;

import java.util.ArrayList;
import java.util.List;

@GodotClass(name = "MPBomberBomb", parent = "Area2D")
public class MPBomberBomb extends Area2D {

    private List<Godot> inArea = new ArrayList<>();
    public int fromPlayer = 0;

    @Override
    public void _ready() {
        // Connect body_entered/body_exited signals (moved from .tscn [connection] lines)
        call("connect", "body_entered", new org.godot.core.Callable(this, "_on_bomb_body_enter"));
        call("connect", "body_exited", new org.godot.core.Callable(this, "_on_bomb_body_exit"));
    }

    @GodotMethod
    public void explode() {
        if (!(boolean) call("is_multiplayer_authority")) return;

        for (Godot p : inArea) {
            if ((boolean) p.call("has_method", "exploded")) {
                Godot world2d = (Godot) call("get_world_2d");
                Godot worldState = (Godot) world2d.call("get", "direct_space_state");
                org.godot.math.Vector2 pos = (org.godot.math.Vector2) getProperty("position");
                org.godot.math.Vector2 pPos = (org.godot.math.Vector2) p.getProperty("position");
                Godot query = (Godot) call("PhysicsRayQueryParameters2D.create", pos, pPos);
                query.setProperty("hit_from_inside", true);
                Object resultObj = worldState.call("intersect_ray", query);
                // intersect_ray returns a Dictionary; extract collider
                java.util.Map<?, ?> result = (java.util.Map<?, ?>) resultObj;
                Object collider = result.get("collider");
                if (collider instanceof Godot) {
                    String colliderType = (String) ((Godot) collider).call("get_class");
                    if (!"TileMapLayer".equals(colliderType) && !"TileMap".equals(colliderType)) {
                        p.call("rpc", "exploded", fromPlayer);
                    }
                }
            }
        }
    }

    @GodotMethod
    public void done() {
        if ((boolean) call("is_multiplayer_authority")) {
            call("queue_free");
        }
    }

    @GodotMethod
    public void _on_bomb_body_enter(Object body) {
        if (body instanceof Godot && !inArea.contains(body)) {
            inArea.add((Godot) body);
        }
    }

    @GodotMethod
    public void _on_bomb_body_exit(Object body) {
        if (body instanceof Godot) {
            inArea.remove(body);
        }
    }
}
