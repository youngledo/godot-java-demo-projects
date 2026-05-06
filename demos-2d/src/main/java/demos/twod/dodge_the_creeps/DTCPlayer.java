package demos.twod.dodge_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector2;
import org.godot.node.Area2D;
import org.godot.node.Node;
import org.godot.node.Viewport;
import org.godot.singleton.Input;

@GodotClass(name = "DTCPlayer", parent = "Area2D")
public class DTCPlayer extends Area2D {

	@Export
	public double speed = 400.0;

	private org.godot.node.AnimatedSprite2D animatedSprite;
	private org.godot.node.CollisionShape2D collisionShape;
	private org.godot.node.Node trail;
	private Vector2 screenSize;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		call("add_user_signal", "hit");
		animatedSprite = (org.godot.node.AnimatedSprite2D) getNode("AnimatedSprite2D");
		collisionShape = (org.godot.node.CollisionShape2D) getNode("CollisionShape2D");
		trail = getNode("Trail");

		org.godot.node.Viewport viewport = getViewport();
		if (viewport != null) {
			Object rect = viewport.getVisibleRect();
			if (rect != null) {
				Object size = ((org.godot.Godot) rect).getProperty("size");
				screenSize = (Vector2) size;
			}
		}

		// Connect body_entered signal
		connect("body_entered", new Callable(this, "_on_body_entered"), 0);
		hide();
	}

	@Override
	public void _process(double delta) {
		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
		double vx = 0, vy = 0;

		if ((boolean) input.isActionPressed("move_right")) vx += 1;
		if ((boolean) input.isActionPressed("move_left")) vx -= 1;
		if ((boolean) input.isActionPressed("move_down")) vy += 1;
		if ((boolean) input.isActionPressed("move_up")) vy -= 1;

		Vector2 pos = (Vector2) getProperty("position");

		if (vx != 0 || vy != 0) {
			double len = Math.sqrt(vx * vx + vy * vy);
			vx /= len; vy /= len;
			if (animatedSprite != null) animatedSprite.play();
		} else {
			if (animatedSprite != null) animatedSprite.stop();
		}

		if (pos != null) {
			double newX = pos.getX() + vx * speed * delta;
			double newY = pos.getY() + vy * speed * delta;
			if (screenSize != null) {
				newX = Math.max(0, Math.min(screenSize.getX(), newX));
				newY = Math.max(0, Math.min(screenSize.getY(), newY));
			}
			setProperty("position", new Vector2(newX, newY));
		}

		if (vx != 0 && animatedSprite != null) {
			animatedSprite.call("set_animation", "right");
			animatedSprite.setProperty("flip_v", false);
			if (trail != null) setProperty("rotation", 0);
			animatedSprite.setProperty("flip_h", vx < 0);
		} else if (vy != 0) {
			if (animatedSprite != null) animatedSprite.call("set_animation", "up");
			setProperty("rotation", vy > 0 ? Math.PI : 0);
		}
	}

	@GodotMethod
	public void start(Vector2 pos) {
		setProperty("position", pos);
		setProperty("rotation", 0);
		show();
		if (collisionShape != null) collisionShape.setProperty("disabled", false);
	}

	@GodotMethod
	public void OnBodyEntered(Object body) {
		hide();
		emitSignal("hit");
		if (collisionShape != null) collisionShape.setDeferred("disabled", true);
	}
}
