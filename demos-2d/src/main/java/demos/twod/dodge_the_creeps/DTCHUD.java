package demos.twod.dodge_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.CanvasLayer;
import org.godot.node.Node;

@GodotClass(name = "DTCHUD", parent = "CanvasLayer")
public class DTCHUD extends CanvasLayer {

	private org.godot.node.Label messageLabel;
	private org.godot.node.Node messageTimer;
	private org.godot.node.Button startButton;
	private org.godot.node.Label scoreLabel;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		call("add_user_signal", "start_game");
		messageLabel = (org.godot.node.Label) getNode("MessageLabel");
		messageTimer = getNode("MessageTimer");
		startButton = (org.godot.node.Button) getNode("StartButton");
		scoreLabel = (org.godot.node.Label) getNode("ScoreLabel");

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
			messageLabel.show();
		}
		if (messageTimer != null) messageTimer.call("start");
	}

	@GodotMethod
	public void showGameOver() {
		showMessage("Game Over");
		// Simplified: no await, just show restart after delay
		if (messageLabel != null) {
			messageLabel.setProperty("text", "Dodge the\nCreeps");
			messageLabel.show();
		}
		if (startButton != null) startButton.show();
	}

	@GodotMethod
	public void updateScore(long newScore) {
		if (scoreLabel != null) {
			scoreLabel.setProperty("text", String.valueOf(newScore));
		}
	}

	@GodotMethod
	public void OnStartButtonPressed() {
		if (startButton != null) startButton.hide();
		emitSignal("start_game");
	}

	@GodotMethod
	public void OnMessageTimerTimeout() {
		if (messageLabel != null) messageLabel.hide();
	}
}
