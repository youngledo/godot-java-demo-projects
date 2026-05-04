package demos.viewport.twod_in_threed;

import java.util.Random;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.Node2D;
import org.godot.node.Sprite2D;
import org.godot.singleton.Input;

@GodotClass(name = "VPPongGame", parent = "Node2D")
public class VPPongGame extends Node2D {

    private static final double PAD_SPEED = 150.0;
    private static final double INITIAL_BALL_SPEED = 80.0;

    private double ballSpeed = INITIAL_BALL_SPEED;
    private Vector2 screenSize = new Vector2(640, 400);
    private Vector2 direction = new Vector2(-1, 0);
    private Vector2 padSize = new Vector2(8, 32);

    private Sprite2D ball;
    private Sprite2D leftPaddle;
    private Sprite2D rightPaddle;
    private boolean initialized = false;
    private final Random random = new Random();

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object viewportRect = call("get_viewport_rect");
        if (viewportRect != null) {
            Object size = ((org.godot.Godot) viewportRect).getProperty("size");
            if (size instanceof Vector2) {
                screenSize = (Vector2) size;
            }
        }

        ball = (Sprite2D) call("get_node", "Ball");
        leftPaddle = (Sprite2D) call("get_node", "LeftPaddle");
        rightPaddle = (Sprite2D) call("get_node", "RightPaddle");

        if (leftPaddle != null) {
            Object tex = leftPaddle.call("get_texture");
            if (tex != null) {
                Object texSize = ((org.godot.Godot) tex).call("get_size");
                if (texSize instanceof Vector2) {
                    padSize = (Vector2) texSize;
                }
            }
        }
    }

    @Override
    public void _process(double delta) {
        if (ball == null || leftPaddle == null || rightPaddle == null) return;

        // Get ball position and pad rectangles
        Vector2 ballPos = (Vector2) ball.call("get_position");
        Vector2 leftPos = (Vector2) leftPaddle.call("get_position");
        Vector2 rightPos = (Vector2) rightPaddle.call("get_position");

        // Integrate new ball position
        ballPos = new Vector2(
            ballPos.getX() + direction.getX() * ballSpeed * delta,
            ballPos.getY() + direction.getY() * ballSpeed * delta
        );

        // Flip when touching roof or floor
        if ((ballPos.getY() < 0 && direction.getY() < 0) || (ballPos.getY() > screenSize.getY() && direction.getY() > 0)) {
            direction = new Vector2(direction.getX(), -direction.getY());
        }

        // Check paddle collisions
        double leftRectX = leftPos.getX() - padSize.getX() * 0.5;
        double leftRectY = leftPos.getY() - padSize.getY() * 0.5;
        double rightRectX = rightPos.getX() - padSize.getX() * 0.5;
        double rightRectY = rightPos.getY() - padSize.getY() * 0.5;

        boolean hitLeft = pointInRect(ballPos, leftRectX, leftRectY, padSize.getX(), padSize.getY()) && direction.getX() < 0;
        boolean hitRight = pointInRect(ballPos, rightRectX, rightRectY, padSize.getX(), padSize.getY()) && direction.getX() > 0;

        if (hitLeft || hitRight) {
            direction = new Vector2(-direction.getX(), direction.getY());
            ballSpeed *= 1.1;
            double randY = random.nextDouble() * 2.0 - 1.0;
            double len = Math.sqrt(direction.getX() * direction.getX() + randY * randY);
            direction = new Vector2(direction.getX() / len, randY / len);
        }

        // Check gameover
        if (ballPos.getX() < 0 || ballPos.getX() > screenSize.getX()) {
            ballPos = new Vector2(screenSize.getX() * 0.5, screenSize.getY() * 0.5);
            ballSpeed = INITIAL_BALL_SPEED;
            direction = new Vector2(-1, 0);
        }

        ball.call("set_position", ballPos);

        // Move left pad
        Input input = Input.singleton();
        if (leftPos.getY() > 0 && (boolean) input.call("is_action_pressed", "left_move_up", false)) {
            leftPos = new Vector2(leftPos.getX(), leftPos.getY() - PAD_SPEED * delta);
        }
        if (leftPos.getY() < screenSize.getY() && (boolean) input.call("is_action_pressed", "left_move_down", false)) {
            leftPos = new Vector2(leftPos.getX(), leftPos.getY() + PAD_SPEED * delta);
        }
        leftPaddle.call("set_position", leftPos);

        // Move right pad
        if (rightPos.getY() > 0 && (boolean) input.call("is_action_pressed", "right_move_up", false)) {
            rightPos = new Vector2(rightPos.getX(), rightPos.getY() - PAD_SPEED * delta);
        }
        if (rightPos.getY() < screenSize.getY() && (boolean) input.call("is_action_pressed", "right_move_down", false)) {
            rightPos = new Vector2(rightPos.getX(), rightPos.getY() + PAD_SPEED * delta);
        }
        rightPaddle.call("set_position", rightPos);
    }

    private boolean pointInRect(Vector2 point, double rx, double ry, double rw, double rh) {
        return point.getX() >= rx && point.getX() <= rx + rw &&
               point.getY() >= ry && point.getY() <= ry + rh;
    }
}
