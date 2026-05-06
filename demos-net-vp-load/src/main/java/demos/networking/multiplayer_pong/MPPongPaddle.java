package demos.networking.multiplayer_pong;

import org.godot.Godot;
import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.Area2D;

@GodotClass(name = "MPPongPaddle", parent = "Area2D")
public class MPPongPaddle extends Area2D {

    private static final double MOTION_SPEED = 150.0;

    @Export
    public boolean left = false;

    private double motion = 0.0;
    private boolean youHidden = false;
    private double screenSizeY;

    @Override
    public void _ready() {
        Godot rect = (Godot) call("get_viewport_rect");
        Vector2 size = (Vector2) rect.getProperty("size");
        screenSizeY = size.getY();
    }

    @Override
    public void _process(double delta) {
        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();

        if ((boolean) call("is_multiplayer_authority")) {
            double upVal = (double) input.getAxis("move_up", "move_down");
            motion = -upVal; // get_axis returns positive for first arg

            if (!youHidden && motion != 0) {
                hideYouLabel();
            }

            motion *= MOTION_SPEED;

            // Using unreliable RPC
            Vector2 pos = (Vector2) getProperty("position");
            call("rpc", "set_pos_and_motion", pos, motion);
        } else {
            if (!youHidden) {
                hideYouLabel();
            }
        }

        Vector2 pos = (Vector2) getProperty("position");
        double newY = pos.getY() + motion * delta;
        newY = Math.max(16, Math.min(screenSizeY - 16, newY));
        setProperty("position", new Vector2(pos.getX(), newY));
    }

    @GodotMethod
    public void setPosAndMotion(Vector2 pos, double motionVal) {
        setProperty("position", pos);
        motion = motionVal;
    }

    private void hideYouLabel() {
        youHidden = true;
        Godot youLabel = (Godot) getNode("You");
        youLabel.call("hide");
    }

    @GodotMethod
    public void OnPaddleAreaEnter(Object area) {
        if ((boolean) call("is_multiplayer_authority")) {
            double randVal = Math.random();
            ((Godot) area).call("rpc", "bounce", left, randVal);
        }
    }
}
