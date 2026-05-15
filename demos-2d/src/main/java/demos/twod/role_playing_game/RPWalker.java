package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node2D;
import org.godot.math.Vector2;
import org.godot.node.Node;

@GodotClass(name = "RPWalker", parent = "Node2D")
public class RPWalker extends RPPawn {

    protected boolean lost = false;
    protected double gridSize = 64.0;
    protected RPGrid grid;
    protected org.godot.node.Node animationTree;
    protected org.godot.node.AnimationPlayer animationPlayer;
    protected org.godot.node.AnimatedSprite2D pose;
    protected double walkAnimationTime = 0.3;

    private boolean moving = false;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        super._ready();

        grid = (RPGrid) getParent();

        animationTree = getNode("AnimationTree");
        animationPlayer = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
        pose = (org.godot.node.AnimatedSprite2D) getNode("Pivot/Slime");

        if (animationPlayer != null) {
            Object walkAnim = animationPlayer.getAnimation("walk");
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
        org.godot.node.Node facingDir = getNode("Pivot/FacingDirection");
        if (facingDir != null) {
            facingDir.setProperty("rotation", direction.angle());
        }
    }

    public void moveTo(Vector2 targetPosition) {
        if (moving) return;
        moving = true;
        setProcess(false);

        org.godot.math.Vector2 pos = (org.godot.math.Vector2) getProperty("position");
        org.godot.math.Vector2 moveDirection = targetPosition.sub(pos).normalized();

        if (pose != null) pose.play("idle");
        if (animationTree != null) {
            Object playback = animationTree.get("parameters/playback");
            if (playback instanceof org.godot.Godot) {
                ((org.godot.node.AnimationNodeStateMachinePlayback) playback).start("walk");
            }
        }

        org.godot.node.Node pivot = getNode("Pivot");
        if (pivot == null) { moving = false; return; }

        Object pivotPosObj = pivot.getProperty("position");
        Vector2 pivotPos = pivotPosObj instanceof Vector2 ? (Vector2) pivotPosObj : Vector2.ZERO;
        Vector2 end = pivotPos.add(moveDirection.mul(gridSize));

        // Create tween for movement
        org.godot.node.Tween tween = createTween();
        if (tween != null) {
            tween.setEase(org.godot.node.Tween.EaseType.EASE_IN);
            tween.tweenProperty(pivot, "position", end, walkAnimationTime);
            tween.connect("finished", new org.godot.core.Callable(this, "on_move_complete"), 0);
            // Store target position for later
            setProperty("_move_target", targetPosition);
        }
    }

    @GodotMethod
    public void onMoveComplete() {
        org.godot.node.Node pivot = getNode("Pivot");
        if (pivot != null) pivot.setProperty("position", Vector2.ZERO);

        Object targetPosObj = getProperty("_move_target");
        if (targetPosObj instanceof Vector2) {
            setProperty("position", targetPosObj);
        }

        if (animationTree != null) {
            Object playback = animationTree.get("parameters/playback");
            if (playback instanceof org.godot.Godot) {
                ((org.godot.node.AnimationNodeStateMachinePlayback) playback).start("idle");
            }
        }
        if (pose != null) pose.play("idle");

        moving = false;
        setProcess(true);
    }

    public void bump() {
        if (moving) return;
        moving = true;
        setProcess(false);

        if (pose != null) pose.play("bump");
        if (animationTree != null) {
            Object playback = animationTree.get("parameters/playback");
            if (playback instanceof org.godot.Godot) {
                ((org.godot.node.AnimationNodeStateMachinePlayback) playback).start("bump");
            }
        }

        // Connect to animation finished signal
        if (animationTree != null) {
            animationTree.connect("animation_finished", new org.godot.core.Callable(this, "on_bump_complete"), 0);
        }
    }

    @GodotMethod
    public void onBumpComplete(String animName) {
        if (animationTree != null) {
            animationTree.disconnect("animation_finished", new org.godot.core.Callable(this, "on_bump_complete"));
            Object playback = animationTree.get("parameters/playback");
            if (playback instanceof org.godot.Godot) {
                ((org.godot.node.AnimationNodeStateMachinePlayback) playback).start("idle");
            }
        }
        if (pose != null) pose.play("idle");

        moving = false;
        setProcess(true);
    }

    public boolean isMoving() { return moving; }
    public boolean isLost() { return lost; }
}
