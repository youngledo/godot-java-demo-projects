package demos.networking.multiplayer_pong;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.Area2D;

@GodotClass(name = "MPPongBall", parent = "Area2D")
public class MPPongBall extends Area2D {

    private static final double DEFAULT_SPEED = 100.0;

    private Vector2 direction = new Vector2(-1.0, 0.0);
    private boolean stopped = false;
    private double speed = DEFAULT_SPEED;
    private Vector2 screenSize;

    @Override
    public void _ready() {
        Godot rect = (Godot) call("get_viewport_rect");
        screenSize = (Vector2) rect.getProperty("size");
    }

    @Override
    public void _process(double delta) {
        speed += delta;
        if (!stopped) {
            translate(new Vector2(speed * delta * direction.getX(), speed * delta * direction.getY()));
        }

        Vector2 ballPos = (Vector2) getProperty("position");
        if ((ballPos.getY() < 0 && direction.getY() < 0) || (ballPos.getY() > screenSize.getY() && direction.getY() > 0)) {
            direction = new Vector2(direction.getX(), -direction.getY());
        }

        if ((boolean) call("is_multiplayer_authority")) {
            if (ballPos.getX() < 0) {
                Godot parent = (Godot) getParent();
                parent.call("rpc", "update_score", false);
                call("rpc", "_reset_ball", false);
            }
        } else {
            if (ballPos.getX() > screenSize.getX() ) {
                Godot parent = (Godot) getParent();
                parent.call("rpc", "update_score", true);
                call("rpc", "_reset_ball", true);
            }
        }
    }

    @GodotMethod
    public void bounce(boolean left, double random) {
        double dx;
        if (left) {
            dx = Math.abs(direction.getX());
        } else {
            dx = -Math.abs(direction.getX());
        }
        speed *= 1.1;
        double dy = random * 2.0 - 1.0;
        double len = Math.sqrt(dx * dx + dy * dy);
        direction = new Vector2(dx / len, dy / len);
    }

    @GodotMethod
    public void stop() {
        stopped = true;
    }

    @GodotMethod
    public void ResetBall(boolean forLeft) {
        setProperty("position", new Vector2(screenSize.getX() / 2, screenSize.getY() / 2));
        if (forLeft) {
            direction = new Vector2(-1.0, 0.0);
        } else {
            direction = new Vector2(1.0, 0.0);
        }
        speed = DEFAULT_SPEED;
    }
}
