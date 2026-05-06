package demos.threed.squash_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Label;

@GodotClass(name = "STCScoreLabel", parent = "Label")
public class STCScoreLabel extends Label {

	private int score = 0;

	@Override
	public void _ready() {
		setProperty("text", "Score: 0");
	}

	@GodotMethod
	public void OnMobSquashed() {
		score++;
		setProperty("text", "Score: " + score);
	}
}
