package demos.twod.dodge_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.CanvasLayer;

@GodotClass(name = "DTCHUD", parent = "CanvasLayer")
public class DTCHUD extends CanvasLayer {

	private org.godot.Godot messageLabel;
	private org.godot.Godot messageTimer;
	private org.godot.Godot startButton;
	private org.godot.Godot scoreLabel;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		call("add_user_signal", "start_game");
		messageLabel = (org.godot.Godot) call("get_node", "MessageLabel");
		messageTimer = (org.godot.Godot) call("get_node", "MessageTimer");
		startButton = (org.godot.Godot) call("get_node", "StartButton");
		scoreLabel = (org.godot.Godot) call("get_node", "ScoreLabel");

		if (startButton != null) {
			startButton.connect("pressed", new Callable(this, "_on_start_button_pressed"), 0);
		}
		if (messageTimer != null) {
			messageTimer.connect("timeout", new Callable(this, "_on_message_timer_timeout"), 0);
		}
	}

	@GodotMethod
	public void showMessage(String text) {
		if (messageLabel != null) {
			messageLabel.setProperty("text", text);
			messageLabel.call("show");
		}
		if (messageTimer != null) messageTimer.call("start");
	}

	@GodotMethod
	public void showGameOver() {
		showMessage("Game Over");
		// Simplified: no await, just show restart after delay
		if (messageLabel != null) {
			messageLabel.setProperty("text", "Dodge the\nCreeps");
			messageLabel.call("show");
		}
		if (startButton != null) startButton.call("show");
	}

	@GodotMethod
	public void update_score(long newScore) {
		if (scoreLabel != null) {
			scoreLabel.setProperty("text", String.valueOf(newScore));
		}
	}

	@GodotMethod
	public void _on_start_button_pressed() {
		if (startButton != null) startButton.call("hide");
		call("emit_signal", "start_game");
	}

	@GodotMethod
	public void _on_message_timer_timeout() {
		if (messageLabel != null) messageLabel.call("hide");
	}
}
