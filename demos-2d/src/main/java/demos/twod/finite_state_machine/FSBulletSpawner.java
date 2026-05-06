package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.node.Node2D;
import org.godot.math.Vector2;
import org.godot.node.Node;

@GodotClass(name = "FSBulletSpawner", parent = "Node2D")
public class FSBulletSpawner extends Node2D {

    private org.godot.node.PackedScene bulletScene;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.node.PackedScene sceneObj = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://player/bullet/Bullet.tscn");
        if (sceneObj instanceof org.godot.node.PackedScene) bulletScene = (org.godot.node.PackedScene) sceneObj;
    }

    @Override
    public boolean _unhandledInput(Object inputEvent) {
        org.godot.Godot event = (org.godot.Godot) inputEvent;
        if (event != null) {
            Object pressed = event.call("is_action_pressed", "fire");
            if (pressed instanceof Boolean && (Boolean) pressed) {
                fire();
            }
        }
        return false;
    }

    private void fire() {
        org.godot.node.Node cooldownTimer = getNode("CooldownTimer");
        if (cooldownTimer != null) {
            Object stopped = cooldownTimer.call("is_stopped");
            if (!(stopped instanceof Boolean && (Boolean) stopped)) return;
            cooldownTimer.call("start");
        }

        if (bulletScene == null) return;
        org.godot.node.Node newBullet = (org.godot.node.Node) bulletScene.instantiate();
        if (newBullet == null) return;

        Object globalPos = getProperty("global_position");
        if (globalPos instanceof Vector2) {
            newBullet.setProperty("position", globalPos);
        }

        org.godot.Godot owner = (org.godot.Godot) getProperty("owner");
        if (owner != null) {
            Object lookDir = owner.getProperty("look_direction");
            if (lookDir instanceof Vector2) {
                newBullet.setProperty("direction", lookDir);
            }
        }

        addChild(newBullet);
    }
}
