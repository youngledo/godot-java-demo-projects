package demos.threed.labels_and_texts;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "Label3DLayout", parent = "Node3D")
public class Label3DLayout extends Node3D {

	private static final int BAR_WIDTH = 100;

	private int health = 0;
	private double counter = 0.0;

	private org.godot.node.Node healthLabel;
	private org.godot.node.Node healthBarFg;
	private org.godot.node.Node healthBarBg;
	private org.godot.node.Node nameNode;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		healthLabel = getNode("Health");
		healthBarFg = getNode("HealthBarForeground");
		healthBarBg = getNode("HealthBarBackground");
		nameNode = getNode("Name");
	}

	@Override
	public void _process(double delta) {
		counter += delta;
		setHealth((int) Math.round(50 + Math.sin(counter * 0.5) * 50));
	}

	@GodotMethod
	public void OnLineEditTextChanged(String newText) {
		if (nameNode != null) {
			nameNode.setProperty("text", newText);
		}
	}

	private void setHealth(int h) {
		health = h;
		if (healthLabel == null) return;

		healthLabel.setProperty("text", health + "%");

		Color healthColor, outlineColor;
		if (health <= 30) {
			healthColor = new Color(1, 0.2, 0.1);
			outlineColor = new Color(0.2, 0.1, 0);
		} else {
			healthColor = new Color(0.8, 1, 0.4);
			outlineColor = new Color(0.15, 0.2, 0.15);
		}

		healthLabel.setProperty("modulate", healthColor);
		healthLabel.setProperty("outline_modulate", outlineColor);
		if (healthBarFg != null) {
			healthBarFg.setProperty("modulate", healthColor);
			healthBarFg.setProperty("outline_modulate", outlineColor);
		}
		if (healthBarBg != null) {
			healthBarBg.setProperty("outline_modulate", outlineColor);
			healthBarBg.setProperty("modulate", outlineColor);
		}

		// Build bar text
		StringBuilder barText = new StringBuilder();
		int bars = (int) Math.round((health / 100.0) * BAR_WIDTH);
		for (int i = 0; i < bars; i++) barText.append("|");

		StringBuilder barBg = new StringBuilder();
		for (int i = 0; i < BAR_WIDTH; i++) barBg.append("|");

		if (healthBarFg != null) healthBarFg.setProperty("text", barText.toString());
		if (healthBarBg != null) healthBarBg.setProperty("text", barBg.toString());
	}
}
