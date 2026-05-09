package demos.networking.multiplayer_bomber;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.AnimationPlayer;
import org.godot.node.CanvasItem;
import org.godot.node.CharacterBody2D;
import org.godot.node.Label;
import org.godot.node.MultiplayerAPI;
import org.godot.node.MultiplayerSynchronizer;

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
    private MPBomberPlayerControls inputs;

    @Override
    public void _ready() {
        stunned = false;
        setPosition(syncedPosition);
        inputs = getNodeAs("Inputs", MPBomberPlayerControls.class);

        String nameStr = (String) getProperty("name");
        try {
            int nameInt = Integer.parseInt(nameStr);
            MultiplayerSynchronizer inputsSync = getNodeAs("Inputs/InputsSync", MultiplayerSynchronizer.class);
            inputsSync.setMultiplayerAuthority(nameInt);
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public void _physicsProcess(double delta) {
        MultiplayerAPI mp = getMultiplayer();
        Object mpPeer = mp.getProperty("multiplayer_peer");
        String nameStr = (String) getProperty("name");
        int uniqueId = mp.getUniqueId();

        if (mpPeer == null || String.valueOf(uniqueId).equals(nameStr)) {
            inputs.update();
        }

        if (mpPeer == null || isMultiplayerAuthority()) {
            Vector2 pos = getPosition();
            syncedPosition = pos;
            lastBombTime += delta;

            boolean isBombing = inputs.bombing;
            if (!stunned && isMultiplayerAuthority() && isBombing && lastBombTime >= BOMB_RATE) {
                lastBombTime = 0.0;
                MPBomberBombSpawner bombSpawner = getNodeAs("../../BombSpawner", MPBomberBombSpawner.class);
                Vector2 position = getPosition();
                Object[] spawnData = new Object[]{position, Integer.parseInt(nameStr)};
                bombSpawner.spawnData(spawnData);
            }
        } else {
            setPosition(syncedPosition);
        }

        if (!stunned) {
            Vector2 motion = inputs.motion;
            double mx = motion != null ? motion.getX() : 0;
            double my = motion != null ? motion.getY() : 0;
            setVelocity(new Vector2(mx * MOTION_SPEED, my * MOTION_SPEED));
            moveAndSlide();
        }

        Vector2 motion = inputs.motion;
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
            AnimationPlayer anim = getNodeAs("anim", AnimationPlayer.class);
            anim.play(currentAnim);
        }
    }

    @GodotMethod
    public void setPlayerName(String value) {
        Label label = getNodeAs("label", Label.class);
        label.setText(value);
        MPBomberGameState gamestate = getNodeAs("/root/gamestate", MPBomberGameState.class);
        Object color = gamestate.getPlayerColor(value);
        label.setProperty("modulate", color);
        CanvasItem sprite = getNodeAs("sprite", CanvasItem.class);
        sprite.setModulate(new org.godot.math.Color(0.5, 0.5, 0.5));
    }

    @GodotMethod
    public void exploded(int byWho) {
        if (stunned) return;
        stunned = true;
        AnimationPlayer anim = getNodeAs("anim", AnimationPlayer.class);
        anim.play("stunned");
    }
}
