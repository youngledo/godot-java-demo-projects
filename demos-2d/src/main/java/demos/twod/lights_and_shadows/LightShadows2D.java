package demos.twod.lights_and_shadows;

import org.godot.annotation.GodotClass;
import org.godot.node.Node2D;
import org.godot.node.Node;
import org.godot.node.SceneTree;

@GodotClass(name = "LightShadows2D", parent = "Node2D")
public class LightShadows2D extends Node2D {

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;

		if ((boolean) ev.isActionPressed("toggle_directional_light")) {
			org.godot.node.Node dirLight = getNode("DirectionalLight2D");
			if (dirLight != null) dirLight.setProperty("visible", !(boolean) dirLight.getProperty("visible"));
			return true;
		}

		if ((boolean) ev.isActionPressed("toggle_point_lights")) {
			org.godot.node.SceneTree tree = getTree();
			if (tree != null) {
				Object[] lights = (Object[]) tree.getNodesInGroup("point_light");
				if (lights != null && lights.length > 0) {
					boolean visible = (boolean) ((org.godot.Godot) lights[0]).getProperty("visible");
					for (Object l : lights) {
						((org.godot.Godot) l).setProperty("visible", !visible);
					}
				}
			}
			return true;
		}

		if ((boolean) ev.isActionPressed("cycle_directional_light_shadows_quality")) {
			org.godot.node.Node dirLight = getNode("DirectionalLight2D");
			if (dirLight != null) {
				long filter = (long) dirLight.getProperty("shadow_filter");
				dirLight.setProperty("shadow_filter", (filter + 1) % 3);
			}
			return true;
		}

		if ((boolean) ev.isActionPressed("cycle_point_light_shadows_quality")) {
			org.godot.node.SceneTree tree = getTree();
			if (tree != null) {
				Object[] lights = (Object[]) tree.getNodesInGroup("point_light");
				if (lights != null) {
					for (Object l : lights) {
						org.godot.Godot light = (org.godot.Godot) l;
						long filter = (long) light.getProperty("shadow_filter");
						light.setProperty("shadow_filter", (filter + 1) % 3);
					}
				}
			}
			return true;
		}
		return false;
	}
}
