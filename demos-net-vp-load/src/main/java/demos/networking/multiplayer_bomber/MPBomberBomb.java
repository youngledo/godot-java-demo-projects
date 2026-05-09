package demos.networking.multiplayer_bomber;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.collection.GodotDictionary;
import org.godot.node.Area2D;
import org.godot.node.Node2D;
import org.godot.node.PhysicsDirectSpaceState2D;
import org.godot.node.PhysicsRayQueryParameters2D;
import org.godot.node.World2D;

import java.util.ArrayList;
import java.util.List;

@GodotClass(name = "MPBomberBomb", parent = "Area2D")
public class MPBomberBomb extends Area2D {

    private List<Node2D> inArea = new ArrayList<>();
    public int fromPlayer = 0;

    @Override
    public void _ready() {
        // Connect body_entered/body_exited signals (moved from .tscn [connection] lines)
        connect("body_entered", new org.godot.core.Callable(this, "_on_bomb_body_enter"));
        connect("body_exited", new org.godot.core.Callable(this, "_on_bomb_body_exit"));
    }

    @GodotMethod
    public void explode() {
        if (!isMultiplayerAuthority()) return;

        for (Node2D p : inArea) {
            if (p.hasMethod("exploded")) {
                World2D world2d = getWorld2d();
                PhysicsDirectSpaceState2D worldState = world2d.getDirectSpaceState();
                org.godot.math.Vector2 pos = getPosition();
                org.godot.math.Vector2 pPos = p.getPosition();
                PhysicsRayQueryParameters2D query = PhysicsRayQueryParameters2D.create(pos, pPos);
                query.setHitFromInside(true);
                GodotDictionary result = worldState.intersectRay(query);
                Object collider = result.get("collider");
                if (collider instanceof org.godot.node.Object colliderObj) {
                    if (!colliderObj.isClass("TileMapLayer") && !colliderObj.isClass("TileMap")) {
                        p.rpc("exploded", fromPlayer);
                    }
                }
            }
        }
    }

    @GodotMethod
    public void done() {
        if (isMultiplayerAuthority()) {
            queueFree();
        }
    }

    @GodotMethod
    public void OnBombBodyEnter(Node2D body) {
        if (!inArea.contains(body)) {
            inArea.add(body);
        }
    }

    @GodotMethod
    public void OnBombBodyExit(Node2D body) {
        inArea.remove(body);
    }
}
