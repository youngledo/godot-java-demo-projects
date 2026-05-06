package demos.threed.tonemap_color_correction;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;

@GodotClass(name = "TCCGradientsControls", parent = "Node")
public class TCCGradientsControls extends Node {

	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;
	}

	@GodotMethod
	public void onStepsValueChanged(double value) {
		setShaderParamOnAllBars("steps", value);
	}

	@GodotMethod
	public void onExponentialToggled(boolean pressed) {
		setShaderParamOnAllBars("exponential_view", pressed);
	}

	private void setShaderParamOnAllBars(String param, Object value) {
		// Iterate children looking for TCCGradientBars nodes
		long childCount = (long) getChildCount();
		for (int i = 0; i < childCount; i++) {
			org.godot.node.Node child = getChild(i);
			if (child == null) continue;
			String className = child.get_class_();
			if ("TCCGradientBars".equals(className)) {
				org.godot.Godot hdrBar = (org.godot.Godot) child.getProperty("hdr_bar");
				if (hdrBar != null) {
					org.godot.Godot mat = (org.godot.Godot) hdrBar.getProperty("material_override");
					if (mat != null) mat.call("set_shader_parameter", param, value);
				}
				org.godot.Godot sdrBar = (org.godot.Godot) child.getProperty("sdr_bar");
				if (sdrBar != null) {
					org.godot.Godot mat = (org.godot.Godot) sdrBar.getProperty("material_override");
					if (mat != null) mat.call("set_shader_parameter", param, value);
				}
			}
		}
	}
}
