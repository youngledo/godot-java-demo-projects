package demos.twod.particles;

import org.godot.annotation.GodotClass;
import org.godot.node.Label;

@GodotClass(name = "ParticleController", parent = "Label")
public class ParticleController extends Label {

	private boolean isCompatibility = false;

	@Override
	public void _ready() {
		String method = (String) call("RenderingServer.get_current_rendering_method");
		if ("gl_compatibility".equals(method)) {
			isCompatibility = true;
			call("set_text", "Space: Pause/Resume\nG: Toggle glow\n\n\n");
		}
	}

	@Override
	public boolean _input(Object inputEvent) {
		// Toggle pause
		boolean togglePause = (boolean) call("Input.is_action_just_pressed", "toggle_pause");
		if (togglePause) {
			org.godot.Godot tree = (org.godot.Godot) call("get_tree");
			boolean paused = (boolean) tree.call("is_paused");
			tree.call("set_pause", !paused);
			return true;
		}

		// Toggle glow
		boolean toggleGlow = (boolean) call("Input.is_action_just_pressed", "toggle_glow");
		if (toggleGlow) {
			org.godot.Godot worldEnv = (org.godot.Godot) call("get_node", "../..");
			if (worldEnv != null) {
				Object env = worldEnv.getProperty("environment");
				if (env != null) {
					org.godot.Godot envObj = (org.godot.Godot) env;
					boolean glowEnabled = (boolean) envObj.getProperty("glow_enabled");
					envObj.setProperty("glow_enabled", !glowEnabled);
				}
			}
			return true;
		}

		return false;
	}
}
