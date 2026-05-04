package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node2D;
import org.godot.math.Vector2;

@GodotClass(name = "RPWalker", parent = "Node2D")
public class RPWalker extends RPPawn {

    protected boolean lost = false;
    protected double gridSize = 64.0;
    protected org.godot.Godot grid;
    protected org.godot.Godot animationTree;
    protected org.godot.Godot animationPlayer;
    protected org.godot.Godot pose;
    protected double walkAnimationTime = 0.3;

    private boolean moving = false;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        super._ready();

        grid = (org.godot.Godot) call("get_parent");

        animationTree = (org.godot.Godot) call("get_node", "AnimationTree");
        animationPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
        pose = (org.godot.Godot) call("get_node", "Pivot/Slime");

        if (animationPlayer != null) {
            Object walkAnim = animationPlayer.call("get_animation", "walk");
            if (walkAnim instanceof org.godot.Godot) {
                Object len = ((org.godot.Godot) walkAnim).getProperty("length");
                if (len instanceof Number) walkAnimationTime = ((Number) len).doubleValue();
            }
        }

        if (grid != null) {
            Object tileSet = grid.getProperty("tile_set");
            if (tileSet instanceof org.godot.Godot) {
                Object tileSize = ((org.godot.Godot) tileSet).getProperty("tile_size");
                if (tileSize instanceof Vector2) {
                    gridSize = ((Vector2) tileSize).x;
                }
            }
        }

        Object poseAnimsObj = getProperty("pose_anims");
        if (poseAnimsObj instanceof org.godot.Godot && pose != null) {
            pose.setProperty("sprite_frames", poseAnimsObj);
        }

        updateLookDirection(new Vector2(1, 0));
    }

    public void updateLookDirection(Vector2 direction) {
        org.godot.Godot facingDir = (org.godot.Godot) call("get_node", "Pivot/FacingDirection");
        if (facingDir != null) {
            facingDir.setProperty("rotation", direction.angle());
        }
    }

    public void moveTo(Vector2 targetPosition) {
        if (moving) return;
        moving = true;
        call("set_process", false);

        org.godot.math.Vector2 pos = (org.godot.math.Vector2) getProperty("position");
        org.godot.math.Vector2 moveDirection = targetPosition.sub(pos).normalized();

        if (pose != null) pose.call("play", "idle");
        if (animationTree != null) {
            Object playback = animationTree.call("get", "parameters/playback");
            if (playback instanceof org.godot.Godot) {
                ((org.godot.Godot) playback).call("start", "walk");
            }
        }

        org.godot.Godot pivot = (org.godot.Godot) call("get_node", "Pivot");
        if (pivot == null) { moving = false; return; }

        Object pivotPosObj = pivot.getProperty("position");
        Vector2 pivotPos = pivotPosObj instanceof Vector2 ? (Vector2) pivotPosObj : Vector2.ZERO;
        Vector2 end = pivotPos.add(moveDirection.mul(gridSize));

        // Create tween for movement
        org.godot.Godot tween = (org.godot.Godot) call("create_tween");
        if (tween != null) {
            tween.call("set_ease", 1); // EASE_IN
            tween.call("tween_property", pivot, "position", end, walkAnimationTime);
            tween.call("connect", "finished", new org.godot.core.Callable(this, "on_move_complete"));
            // Store target position for later
            setProperty("_move_target", targetPosition);
        }
    }

    @GodotMethod
    public void on_move_complete() {
        org.godot.Godot pivot = (org.godot.Godot) call("get_node", "Pivot");
        if (pivot != null) pivot.setProperty("position", Vector2.ZERO);

        Object targetPosObj = getProperty("_move_target");
        if (targetPosObj instanceof Vector2) {
            setProperty("position", targetPosObj);
        }

        if (animationTree != null) {
            Object playback = animationTree.call("get", "parameters/playback");
            if (playback instanceof org.godot.Godot) {
                ((org.godot.Godot) playback).call("start", "idle");
            }
        }
        if (pose != null) pose.call("play", "idle");

        moving = false;
        call("set_process", true);
    }

    public void bump() {
        if (moving) return;
        moving = true;
        call("set_process", false);

        if (pose != null) pose.call("play", "bump");
        if (animationTree != null) {
            Object playback = animationTree.call("get", "parameters/playback");
            if (playback instanceof org.godot.Godot) {
                ((org.godot.Godot) playback).call("start", "bump");
            }
        }

        // Connect to animation finished signal
        if (animationTree != null) {
            animationTree.call("connect", "animation_finished", new org.godot.core.Callable(this, "on_bump_complete"));
        }
    }

    @GodotMethod
    public void on_bump_complete(String animName) {
        if (animationTree != null) {
            animationTree.call("disconnect", "animation_finished", new org.godot.core.Callable(this, "on_bump_complete"));
            Object playback = animationTree.call("get", "parameters/playback");
            if (playback instanceof org.godot.Godot) {
                ((org.godot.Godot) playback).call("start", "idle");
            }
        }
        if (pose != null) pose.call("play", "idle");

        moving = false;
        call("set_process", true);
    }

    public boolean isMoving() { return moving; }
    public boolean isLost() { return lost; }
}
