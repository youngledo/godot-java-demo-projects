package demos.twod.glow;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.Node2D;
import org.godot.node.Node;

@GodotClass(name = "BeachCave", parent = "Node2D")
public class BeachCave extends Node2D {

	private static final double CAVE_LIMIT = 1000;

	private org.godot.node.Node cave;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;
		cave = getNode("Cave");
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		String className = ev.get_class_();

		if ("InputEventMouseMotion".equals(className)) {
			long buttonMask = (long) ev.getProperty("button_mask");
			if (buttonMask > 0 && cave != null) {
				Vector2 pos = (Vector2) cave.getProperty("position");
				Vector2 relative = (Vector2) ev.getProperty("screen_relative");
				if (pos != null && relative != null) {
					double newX = Math.max(-CAVE_LIMIT, Math.min(0, pos.getX()));
					cave.setProperty("position", new Vector2(newX, pos.getY()));
				}
			}
		}

		if ((boolean) ev.isActionPressed("toggle_glow_map")) {
			org.godot.node.Node worldEnv = getNode("WorldEnvironment");
			if (worldEnv != null) {
				org.godot.Godot env = (org.godot.Godot) worldEnv.getProperty("environment");
				if (env != null) {
					Object glowMap = env.getProperty("glow_map");
					if (glowMap != null) {
						env.setProperty("glow_map", null);
						env.setProperty("glow_intensity", 0.8);
					} else {
						Object map = call("load", "res://glow_map.webp");
						env.setProperty("glow_map", map);
						env.setProperty("glow_intensity", 1.6);
					}
				}
			}
			return true;
		}
		return false;
	}
}
