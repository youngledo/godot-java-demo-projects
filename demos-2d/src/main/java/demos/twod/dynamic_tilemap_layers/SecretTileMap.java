package demos.twod.dynamic_tilemap_layers;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Color;
import org.godot.node.CharacterBody2D;
import org.godot.node.TileMapLayer;
import org.godot.node.Node;

@GodotClass(name = "SecretTileMap", parent = "TileMapLayer")
public class SecretTileMap extends TileMapLayer {

	private boolean playerInSecret = false;
	private double layerAlpha = 1.0;
	private boolean processing = false;

	@Override
	public void _ready() {
		// Connect to secret detector signals
		org.godot.node.Node detector = getNode("../SecretDetector");
		if (detector != null) {
			detector.connect("body_entered", new Callable(this, "_on_secret_detector_body_entered"), 0);
			detector.connect("body_exited", new Callable(this, "_on_secret_detector_body_exited"), 0);
		}
		setProcess(false);
	}

	@Override
	public void _process(double delta) {
		if (playerInSecret) {
			if (layerAlpha > 0.3) {
				layerAlpha = moveToward(layerAlpha, 0.3, delta);
				setProperty("self_modulate", new Color(1, 1, 1, layerAlpha));
			} else {
				setProcess(false);
			}
		} else {
			if (layerAlpha < 1.0) {
				layerAlpha = moveToward(layerAlpha, 1.0, delta);
				setProperty("self_modulate", new Color(1, 1, 1, layerAlpha));
			} else {
				setProcess(false);
			}
		}
	}

	@GodotMethod
	public void OnSecretDetectorBodyEntered(Object body) {
		if (body instanceof CharacterBody2D) {
			playerInSecret = true;
			setProcess(true);
		}
	}

	@GodotMethod
	public void OnSecretDetectorBodyExited(Object body) {
		if (body instanceof CharacterBody2D) {
			playerInSecret = false;
			setProcess(true);
		}
	}

	private static double moveToward(double current, double target, double maxDelta) {
		if (Math.abs(target - current) <= maxDelta) return target;
		return current + Math.signum(target - current) * maxDelta;
	}
}
