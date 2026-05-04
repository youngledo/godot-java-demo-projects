package demos.twod.bullet_shower;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.Node2D;

@GodotClass(name = "BSBullets", parent = "Node2D")
public class BSBullets extends Node2D {

	private static final int BULLET_COUNT = 500;
	private static final double SPEED_MIN = 20;
	private static final double SPEED_MAX = 80;

	private double[] posX = new double[BULLET_COUNT];
	private double[] posY = new double[BULLET_COUNT];
	private double[] speeds = new double[BULLET_COUNT];
	private double viewportWidth = 1152;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		org.godot.Godot viewport = (org.godot.Godot) call("get_viewport");
		if (viewport != null) {
			Object rect = viewport.call("get_visible_rect");
			if (rect != null) {
				Vector2 size = (Vector2) ((org.godot.Godot) rect).getProperty("size");
				if (size != null) viewportWidth = size.getX();
			}
		}

		for (int i = 0; i < BULLET_COUNT; i++) {
			speeds[i] = SPEED_MIN + Math.random() * (SPEED_MAX - SPEED_MIN);
			posX[i] = Math.random() * viewportWidth + viewportWidth; // Start offscreen
			posY[i] = Math.random() * viewportWidth;
		}
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

	// Note: The original uses _draw() to draw bullet textures and PhysicsServer2D
	// for low-level collision. Due to FFI limitations with canvas drawing and
	// PhysicsServer2D RID management, this is a simplified version that just
	// handles movement. Full PhysicsServer2D integration would require
	// additional godot-java API support.
}
