package demos.threed.tonemap_color_correction;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node3D;
import org.godot.node.Node;
import org.godot.node.ShaderMaterial;

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
		setShaderParameter(hdrBar, "steps", Math.min(1, steps));
	}

	@GodotMethod
	public void setColor(org.godot.math.Color color) {
		setShaderParameter(sdrBar, "my_color", color);
		setShaderParameter(hdrBar, "my_color", color);
		if (label != null) {
			String hex = String.format("#%02x%02x%02x",
				(int)(color.r * 255), (int)(color.g * 255), (int)(color.b * 255));
			label.setProperty("text", hex);
		}
	}

	private void setShaderParameter(org.godot.Godot bar, String parameter, Object value) {
		if (bar != null && bar.getProperty("material_override") instanceof ShaderMaterial mat) {
			mat.setShaderParameter(parameter, value);
		}
	}
}
