package demos.twod.tween;

import org.godot.annotation.GodotClass;
import org.godot.math.Color;
import org.godot.math.Vector2;
import org.godot.node.Label;
import org.godot.node.Node2D;
import org.godot.singleton.Input;

@GodotClass(name = "TweenDemo", parent = "Node2D")
public class TweenDemo extends Node2D {

	private org.godot.Godot icon;
	private org.godot.Godot progress;
	private org.godot.Godot tween;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		icon = (org.godot.Godot) call("get_node", "Icon");
		progress = (org.godot.Godot) call("get_node", "CanvasLayer/Progress");
	}

	@Override
	public void _process(double delta) {
		if (tween == null) return;
		boolean running = (boolean) tween.call("is_running");
		if (!running) return;

		if (progress != null) {
			double elapsed = (double) tween.call("get_total_elapsed_time");
			progress.setProperty("value", elapsed);
		}
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		Input input = Input.singleton();

		if (input.is_action_just_pressed("ui_accept", false)) {
			startAnimation();
			return true;
		}
		return false;
	}

	private void startAnimation() {
		reset();

		tween = (org.godot.Godot) call("create_tween");
		if (tween == null) return;

		if (progress != null) {
			progress.setProperty("max_value", 0);
		}

		// Step 1: Move to position
		if (icon != null) {
			double maxVal = 0;
			// Move
			tween.call("tween_property", icon, "position", new Vector2(400, 250), 1.0);
			maxVal += 1.0;
			// Color red
			tween.call("tween_property", icon, "self_modulate", new Color(1, 0, 0), 1.0);
			maxVal += 1.0;
			// Move relative right
			tween.call("tween_property", icon, "position:x", 200.0, 1.0);
			// Roll (parallel)
			tween.call("tween_property", icon, "rotation", Math.PI * 2, 1.0);
			maxVal += 1.0;
			// Scale up
			tween.call("tween_property", icon, "scale", new Vector2(5, 5), 0.5);
			// Vanish
			tween.call("tween_property", icon, "self_modulate:a", 0.0, 1.0);
			maxVal += 2.0;

			if (progress != null) {
				progress.setProperty("max_value", maxVal);
			}
		}

		// Loop back
		tween.call("tween_callback", new org.godot.core.Callable(this, "showIcon"));
	}

	@org.godot.annotation.GodotMethod
	public void showIcon() {
		if (icon != null) {
			icon.call("show");
			icon.setProperty("self_modulate", new Color(1, 1, 1));
		}
	}

	private void reset() {
		if (tween != null) {
			tween.call("kill");
			tween = null;
		}
		if (icon != null) {
			icon.setProperty("position", new Vector2(325, 250));
			icon.setProperty("self_modulate", new Color(1, 1, 1));
			icon.setProperty("rotation", 0.0);
			icon.setProperty("scale", new Vector2(1, 1));
			icon.call("show");
		}
	}
}
