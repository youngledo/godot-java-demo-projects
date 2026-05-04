package demos.twod.isometric;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.CharacterBody2D;
import org.godot.singleton.Input;

@GodotClass(name = "Goblin", parent = "CharacterBody2D")
public class Goblin extends CharacterBody2D {

	private static final double MOTION_SPEED = 160.0;

	private static final String[][] IDLE_ANIMS = {
		{"side_right_idle", "false"}, {"45front_right_idle", "false"}, {"front_idle", "false"},
		{"45front_left_idle", "false"}, {"side_left_idle", "false"}, {"45back_left_idle", "false"},
		{"back_idle", "false"}, {"45back_right_idle", "false"},
	};

	private static final String[][] WALK_ANIMS = {
		{"side_right_walk", "false"}, {"45front_right_walk", "false"}, {"front_walk", "false"},
		{"45front_left_walk", "false"}, {"side_left_walk", "false"}, {"45back_left_walk", "false"},
		{"back_walk", "false"}, {"45back_right_walk", "false"},
	};

	private org.godot.Godot sprite;
	private double spriteScaleX = 1.0;
	private double lastDirX = 1.0;
	private double lastDirY = 0.0;

	@Override
	public void _ready() {
		sprite = (org.godot.Godot) call("get_node", "Sprite2D");
		if (sprite != null) {
			Vector2 scale = (Vector2) sprite.getProperty("scale");
			spriteScaleX = scale.getX();
		}
	}

	@Override
	public void _physicsProcess(double delta) {
		Input input = Input.singleton();
		double mx = input.get_axis("move_left", "move_right");
		double my = input.get_axis("move_up", "move_down") / 2.0; // isometric correction
		double len = Math.sqrt(mx * mx + my * my);
		if (len > 0) {
			mx /= len;
			my /= len;
		}

		Vector2 motion = new Vector2(mx * MOTION_SPEED, my * MOTION_SPEED);
		setProperty("velocity", motion);
		call("move_and_slide");

		Vector2 vel = (Vector2) getProperty("velocity");
		double vx = vel.getX();
		double vy = vel.getY();

		if (Math.abs(vx) > 0.01 || Math.abs(vy) > 0.01) {
			lastDirX = vx;
			lastDirY = vy;
			updateAnimation(WALK_ANIMS);
		} else {
			updateAnimation(IDLE_ANIMS);
		}
	}

	private void updateAnimation(String[][] anims) {
		if (sprite == null) return;

		double angle = Math.toDegrees(Math.atan2(lastDirY, lastDirX)) + 22.5;
		if (angle < 0) angle += 360;
		int slice = (int) Math.floor(angle / 45) % 8;

		sprite.call("play", anims[slice][0]);
		boolean flipH = "true".equals(anims[slice][1]);
		if (!flipH) {
			// Flip based on direction for side animations
			if (slice == 4 || slice == 5) {
				flipH = false;
			} else if (slice == 0 || slice == 7) {
				flipH = false;
			}
		}
		sprite.setProperty("flip_h", flipH);

		// Flip for left-facing
		if (lastDirX < 0) {
			if (slice == 0) {
				sprite.setProperty("flip_h", true);
				sprite.call("play", "side_right_idle".contains("idle") && anims == IDLE_ANIMS ? "side_left_idle" : anims[0][0]);
				sprite.setProperty("flip_h", true);
			}
		}
	}
}
