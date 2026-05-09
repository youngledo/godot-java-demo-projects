package demos.threed.sprites;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.AnimatedSprite3D;
import org.godot.singleton.Input;

@GodotClass(name = "Player3DSprite", parent = "Node3D")
public class Player3DSprite extends Node3D {

	@Export
	public double moveSpeed = 5.0;

	private AnimatedSprite3D sprite;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;
		sprite = getNodeAs("AnimatedSprite3D", AnimatedSprite3D.class);
	}

	@Override
	public void _process(double delta) {
		if (sprite == null) return;
		Input input = Input.singleton();

		double inputX = input.getActionStrength("move_right", false)
				- input.getActionStrength("move_left", false);
		double inputY = input.getActionStrength("move_back", false)
				- input.getActionStrength("move_forward", false);

		if (Math.abs(inputX) > 0.01 || Math.abs(inputY) > 0.01) {
			double len = Math.sqrt(inputX * inputX + inputY * inputY);
			inputX /= len;
			inputY /= len;

			Vector3 velocity = new Vector3(inputX, 0, inputY);
			Vector3 scaled = new Vector3(velocity.getX() * moveSpeed * delta, 0, velocity.getZ() * moveSpeed * delta);
			translate(scaled);

			if (Math.abs(inputX) > Math.abs(inputY)) {
				sprite.play( inputX > 0 ? "walk_right" : "walk_left");
			} else {
				sprite.play( inputY > 0 ? "walk_down" : "walk_up");
			}
		} else {
			sprite.stop();
		}
	}
}
