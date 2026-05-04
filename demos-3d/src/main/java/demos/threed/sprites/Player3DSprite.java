package demos.threed.sprites;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.singleton.Input;

@GodotClass(name = "Player3DSprite", parent = "Node3D")
public class Player3DSprite extends Node3D {

	@Export
	public double moveSpeed = 5.0;

	private org.godot.Godot sprite;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;
		sprite = (org.godot.Godot) call("get_node", "AnimatedSprite3D");
	}

	@Override
	public void _process(double delta) {
		if (sprite == null) return;
		Input input = Input.singleton();

		double inputX = (double) input.call("get_action_strength", "move_right", false)
				- (double) input.call("get_action_strength", "move_left", false);
		double inputY = (double) input.call("get_action_strength", "move_back", false)
				- (double) input.call("get_action_strength", "move_forward", false);

		if (Math.abs(inputX) > 0.01 || Math.abs(inputY) > 0.01) {
			double len = Math.sqrt(inputX * inputX + inputY * inputY);
			inputX /= len;
			inputY /= len;

			Vector3 velocity = new Vector3(inputX, 0, inputY);
			Vector3 scaled = new Vector3(velocity.getX() * moveSpeed * delta, 0, velocity.getZ() * moveSpeed * delta);
			call("translate", scaled);

			if (Math.abs(inputX) > Math.abs(inputY)) {
				sprite.call("play", inputX > 0 ? "walk_right" : "walk_left");
			} else {
				sprite.call("play", inputY > 0 ? "walk_down" : "walk_up");
			}
		} else {
			sprite.call("stop");
		}
	}
}
