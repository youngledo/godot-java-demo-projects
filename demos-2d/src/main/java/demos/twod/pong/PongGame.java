package demos.twod.pong;

import java.util.Random;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.math.Color;
import org.godot.math.Vector2;
import org.godot.node.ColorRect;
import org.godot.node.Label;
import org.godot.node.Node2D;
import org.godot.singleton.Input;
import org.godot.node.Node;

@GodotClass(name = "PongGame", parent = "Node2D")
public class PongGame extends Node2D {

	private static final double WIDTH = 640.0;
	private static final double HEIGHT = 400.0;
	private static final double PADDLE_W = 8.0;
	private static final double PADDLE_H = 32.0;
	private static final double BALL_SIZE = 8.0;
	private static final double PADDLE_OFFSET = 30.0;
	private static final double DEFAULT_SPEED = 100.0;

	@Export
	public double paddleSpeed = 100.0;

	private double ballX, ballY, ballVX, ballVY, ballSpeed;
	private double leftY, rightY;
	private double leftScreenY;
	private final Random random = new Random();
	private boolean initialized;

	private ColorRect ball;
	private ColorRect leftPaddle;
	private ColorRect rightPaddle;
	private Node2D container;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		leftScreenY = HEIGHT;

		// Background
		ColorRect bg = ColorRect.create();
		bg.setColor(new Color(0.141176, 0.152941, 0.164706));
		bg.setSize(new Vector2(WIDTH, HEIGHT));
		addChild(bg, false, 0);

		// Separator
		ColorRect sep = ColorRect.create();
		sep.setColor(new Color(0.3, 0.3, 0.3));
		sep.setSize(new Vector2(2, HEIGHT));
		sep.setPosition(new Vector2(WIDTH / 2 - 1, 0));
		addChild(sep, false, 0);

		// Ball
		ball = ColorRect.create();
		ball.setColor(new Color(1, 1, 1));
		ball.setSize(new Vector2(BALL_SIZE, BALL_SIZE));
		addChild(ball, false, 0);

		// Paddles
		leftPaddle = ColorRect.create();
		leftPaddle.setColor(new Color(0, 1, 1));
		leftPaddle.setSize(new Vector2(PADDLE_W, PADDLE_H));
		addChild(leftPaddle, false, 0);

		rightPaddle = ColorRect.create();
		rightPaddle.setColor(new Color(1, 0, 1));
		rightPaddle.setSize(new Vector2(PADDLE_W, PADDLE_H));
		addChild(rightPaddle, false, 0);

		resetBall(-1);
		leftY = HEIGHT / 2;
		rightY = HEIGHT / 2;
		updatePositions();
	}

	@Override
	public void _process(double delta) {
		Input input = Input.singleton();

		// Left paddle (W/S)
		double leftInput = input.getActionStrength("left_move_down", false) - input.getActionStrength("left_move_up", false);
		leftY = clamp(leftY + leftInput * paddleSpeed * delta, PADDLE_H / 2, leftScreenY - PADDLE_H / 2);

		// Right paddle (Up/Down)
		double rightInput = input.getActionStrength("right_move_down", false) - input.getActionStrength("right_move_up", false);
		rightY = clamp(rightY + rightInput * paddleSpeed * delta, PADDLE_H / 2, leftScreenY - PADDLE_H / 2);

		// Ball movement
		ballSpeed += delta * 2;
		ballX += ballSpeed * delta * ballVX;
		ballY += ballSpeed * delta * ballVY;

		// Bounce off ceiling/floor
		if (ballY - BALL_SIZE / 2 <= 0) {
			ballY = BALL_SIZE / 2;
			ballVY = Math.abs(ballVY);
		} else if (ballY + BALL_SIZE / 2 >= HEIGHT) {
			ballY = HEIGHT - BALL_SIZE / 2;
			ballVY = -Math.abs(ballVY);
		}

		// Left paddle collision
		double lpx = PADDLE_OFFSET;
		if (ballVX < 0 && ballX - BALL_SIZE / 2 <= lpx + PADDLE_W && ballX + BALL_SIZE / 2 >= lpx
					&& ballY >= leftY - PADDLE_H / 2 - BALL_SIZE / 2 && ballY <= leftY + PADDLE_H / 2 + BALL_SIZE / 2) {
			ballX = lpx + PADDLE_W + BALL_SIZE / 2;
			bounceOffPaddle(leftY, 1);
		}

		// Right paddle collision
		double rpx = WIDTH - PADDLE_OFFSET - PADDLE_W;
		if (ballVX > 0 && ballX + BALL_SIZE / 2 >= rpx && ballX - BALL_SIZE / 2 <= rpx + PADDLE_W
					&& ballY >= rightY - PADDLE_H / 2 - BALL_SIZE / 2 && ballY <= rightY + PADDLE_H / 2 + BALL_SIZE / 2) {
			ballX = rpx - BALL_SIZE / 2;
			bounceOffPaddle(rightY, -1);
		}

		// Score (ball out of bounds) - reset
		if (ballX < -BALL_SIZE || ballX > WIDTH + BALL_SIZE) {
			resetBall(ballX < 0 ? -1 : 1);
		}

		updatePositions();
	}

	private void bounceOffPaddle(double paddleYCenter, int directionX) {
		double relativeHit = (ballY - paddleYCenter) / (PADDLE_H / 2);
		double angle = relativeHit * Math.PI / 3;
		ballVX = directionX * Math.cos(angle);
		ballVY = Math.sin(angle);
	}

	private void resetBall(int directionX) {
		ballX = WIDTH / 2;
		ballY = HEIGHT / 2;
		ballSpeed = DEFAULT_SPEED;
		double angle = (random.nextDouble() - 0.5) * Math.PI / 3;
		ballVX = directionX * Math.cos(angle);
		ballVY = Math.sin(angle);
	}

	private void updatePositions() {
		ball.setPosition(new Vector2(ballX - BALL_SIZE / 2, ballY - BALL_SIZE / 2));
		leftPaddle.setPosition(new Vector2(PADDLE_OFFSET, leftY - PADDLE_H / 2));
		rightPaddle.setPosition(new Vector2(WIDTH - PADDLE_OFFSET - PADDLE_W, rightY - PADDLE_H / 2));
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}
