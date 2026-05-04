package demos.threed.tonemap_color_correction;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node3D;

@GodotClass(name = "TCCGradientBars", parent = "Node3D")
public class TCCGradientBars extends Node3D {

	@Export
	public org.godot.Godot sdrBar;
	@Export
	public org.godot.Godot hdrBar;
	@Export
	public org.godot.Godot label;

	@GodotMethod
	public void setNumSteps(int steps) {
		if (hdrBar != null) {
			org.godot.Godot mat = (org.godot.Godot) hdrBar.getProperty("material_override");
			if (mat != null) mat.call("set_shader_parameter", "steps", Math.min(1, steps));
		}
	}

	@GodotMethod
	public void setColor(org.godot.math.Color color) {
		if (sdrBar != null) {
			org.godot.Godot mat = (org.godot.Godot) sdrBar.getProperty("material_override");
			if (mat != null) mat.call("set_shader_parameter", "my_color", color);
		}
		if (hdrBar != null) {
			org.godot.Godot mat = (org.godot.Godot) hdrBar.getProperty("material_override");
			if (mat != null) mat.call("set_shader_parameter", "my_color", color);
		}
		if (label != null) {
			String hex = String.format("#%02x%02x%02x",
				(int)(color.r * 255), (int)(color.g * 255), (int)(color.b * 255));
			label.setProperty("text", hex);
		}
	}
}
