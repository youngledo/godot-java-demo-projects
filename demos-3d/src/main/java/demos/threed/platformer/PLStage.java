package demos.threed.platformer;

import org.godot.annotation.GodotClass;
import org.godot.node.Node3D;

@GodotClass(name = "PLStage", parent = "Node3D")
public class PLStage extends Node3D {

	private boolean initialized = false;
	private org.godot.Godot newLight;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		org.godot.singleton.RenderingServer rs = org.godot.singleton.RenderingServer.singleton();
		if (rs == null) return;

		Object method = rs.call("get_current_rendering_method");
		if (method != null && "gl_compatibility".equals(method.toString())) {
			rs.call("directional_soft_shadow_filter_set_quality", 3);

			org.godot.Godot light = (org.godot.Godot) call("get_node", "DirectionalLight3D");
			if (light != null) {
				light.setProperty("sky_mode", 0);
				newLight = (org.godot.Godot) light.call("duplicate");
				if (newLight != null) {
					newLight.setProperty("light_energy", 0.25);
					newLight.setProperty("sky_mode", 1);
					call("add_child", newLight);
				}
			}
		}
	}

	@Override
	public void _exitTree() {
		if (newLight != null) {
			newLight.call("queue_free");
			newLight = null;
		}
	}
}
