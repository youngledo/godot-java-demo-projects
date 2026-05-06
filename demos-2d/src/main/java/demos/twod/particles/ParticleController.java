package demos.twod.particles;

import org.godot.annotation.GodotClass;
import org.godot.node.Label;
import org.godot.node.Node;
import org.godot.node.SceneTree;
import org.godot.singleton.Input;

@GodotClass(name = "ParticleController", parent = "Label")
public class ParticleController extends Label {

	private boolean isCompatibility = false;

	@Override
	public void _ready() {
		String method = (String) call("RenderingServer.get_current_rendering_method");
		if ("gl_compatibility".equals(method)) {
			isCompatibility = true;
			setText("Space: Pause/Resume\nG: Toggle glow\n\n\n");
		}
	}

	@Override
	public boolean _input(Object inputEvent) {
		// Toggle pause
		boolean togglePause = (boolean) (boolean) Input.singleton().isActionJustPressed( "toggle_pause");
		if (togglePause) {
			org.godot.node.SceneTree tree = getTree();
			boolean paused = (boolean) tree.isPaused();
			tree.setPause(!paused);
			return true;
		}

		// Toggle glow
		boolean toggleGlow = (boolean) (boolean) Input.singleton().isActionJustPressed( "toggle_glow");
		if (toggleGlow) {
			org.godot.node.Node worldEnv = getNode("../..");
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
