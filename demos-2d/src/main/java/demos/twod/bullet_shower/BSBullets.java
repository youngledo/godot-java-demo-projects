package demos.twod.bullet_shower;

import org.godot.annotation.GodotClass;
import org.godot.math.Color;
import org.godot.math.Vector2;
import org.godot.node.Node2D;
import org.godot.node.Viewport;

@GodotClass(name = "BSBullets", parent = "Node2D")
public class BSBullets extends Node2D {

	private static final int BULLET_COUNT = 500;
	private static final double SPEED_MIN = 20;
	private static final double SPEED_MAX = 80;
	private static final double BULLET_RADIUS = 4.0;

	private double[] posX = new double[BULLET_COUNT];
	private double[] posY = new double[BULLET_COUNT];
	private double[] speeds = new double[BULLET_COUNT];
	private double viewportWidth = 1152;
	private double viewportHeight = 648;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		Viewport viewport = getViewport();
		if (viewport != null) {
			org.godot.math.Rect2 rect = viewport.getVisibleRect();
			if (rect != null && rect.size != null) {
				viewportWidth = rect.size.getX();
				viewportHeight = rect.size.getY();
			}
		}

		for (int i = 0; i < BULLET_COUNT; i++) {
			speeds[i] = SPEED_MIN + Math.random() * (SPEED_MAX - SPEED_MIN);
			posX[i] = Math.random() * viewportWidth + viewportWidth;
			posY[i] = Math.random() * viewportHeight;
		}
	}

	@Override
	public void _process(double delta) {
		queueRedraw();
	}

	@Override
	public void _physicsProcess(double delta) {
		double offset = viewportWidth + 16;
		for (int i = 0; i < BULLET_COUNT; i++) {
			posX[i] -= speeds[i] * delta;
			if (posX[i] < -16) {
				posX[i] = offset;
			}
		}
	}

	@Override
	public void _draw() {
		Color color = new Color(1, 1, 0.4, 0.9);
		for (int i = 0; i < BULLET_COUNT; i++) {
			drawCircle(new Vector2(posX[i], posY[i]), BULLET_RADIUS, color);
		}
	}
}
