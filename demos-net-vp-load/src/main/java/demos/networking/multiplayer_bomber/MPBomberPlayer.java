package demos.networking.multiplayer_bomber;

import org.godot.Godot;
import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;

@GodotClass(name = "MPBomberPlayer", parent = "CharacterBody2D")
public class MPBomberPlayer extends CharacterBody2D {

    private static final double MOTION_SPEED = 90.0;
    private static final double BOMB_RATE = 0.5;

    @Export
    public Vector2 syncedPosition = new Vector2(0.0, 0.0);

    @Export
    public boolean stunned = false;

    private double lastBombTime = BOMB_RATE;
    private String currentAnim = "";
    private Godot inputs;

    @Override
    public void _ready() {
        stunned = false;
        setProperty("position", syncedPosition);
        inputs = (Godot) getNode("Inputs");

        String nameStr = (String) getProperty("name");
        try {
            int nameInt = Integer.parseInt(nameStr);
            Godot inputsSync = (Godot) getNode("Inputs/InputsSync");
            inputsSync.call("set_multiplayer_authority", nameInt);
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public void _physicsProcess(double delta) {
        Godot mp = (Godot) getMultiplayer();
        Object mpPeer = mp.getProperty("multiplayer_peer");
        String nameStr = (String) getProperty("name");
        long uniqueId = (long) mp.call("get_unique_id");

        if (mpPeer == null || String.valueOf(uniqueId).equals(nameStr)) {
            inputs.call("update");
        }

        if (mpPeer == null || (boolean) call("is_multiplayer_authority")) {
            Vector2 pos = (Vector2) getProperty("position");
            syncedPosition = pos;
            lastBombTime += delta;

            boolean isBombing = (boolean) inputs.getProperty("bombing");
            if (!stunned && (boolean) call("is_multiplayer_authority") && isBombing && lastBombTime >= BOMB_RATE) {
                lastBombTime = 0.0;
                Godot bombSpawner = (Godot) getNode("../../BombSpawner");
                Vector2 position = (Vector2) getProperty("position");
                Object[] spawnData = new Object[]{position, Integer.parseInt(nameStr)};
                bombSpawner.call("spawn", new Object[]{spawnData});
            }
        } else {
            setProperty("position", syncedPosition);
        }

        if (!stunned) {
            Vector2 motion = (Vector2) inputs.getProperty("motion");
            double mx = motion != null ? motion.getX() : 0;
            double my = motion != null ? motion.getY() : 0;
            setProperty("velocity", new Vector2(mx * MOTION_SPEED, my * MOTION_SPEED));
            moveAndSlide();
        }

        // Update animation
        Vector2 motion = (Vector2) inputs.getProperty("motion");
        double mx = motion != null ? motion.getX() : 0;
        double my = motion != null ? motion.getY() : 0;

        String newAnim = "standing";
        if (my < 0) newAnim = "walk_up";
        else if (my > 0) newAnim = "walk_down";
        else if (mx < 0) newAnim = "walk_left";
        else if (mx > 0) newAnim = "walk_right";

        if (stunned) newAnim = "stunned";

        if (!newAnim.equals(currentAnim)) {
            currentAnim = newAnim;
            Godot anim = (Godot) getNode("anim");
            anim.call("play", currentAnim);
        }
    }

    @GodotMethod
    public void setPlayerName(String value) {
        Godot label = (Godot) getNode("label");
        label.setProperty("text", value);
        Godot gamestate = (Godot) getNode("/root/gamestate");
        Object color = gamestate.call("get_player_color", value);
        label.setProperty("modulate", color);
        Godot sprite = (Godot) getNode("sprite");
        Godot modColor = (Godot) call("Color", 0.5, 0.5, 0.5);
        // Add gamestate color to gray
        sprite.setProperty("modulate", modColor);
    }

    @GodotMethod
    public void exploded(int byWho) {
        if (stunned) return;
        stunned = true;
        Godot anim = (Godot) getNode("anim");
        anim.call("play", "stunned");
    }
}
