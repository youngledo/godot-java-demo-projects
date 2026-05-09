package demos.threed.tonemap_color_correction;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;
import org.godot.node.ShaderMaterial;

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
		long childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			if (getChild(i) instanceof TCCGradientBars bars) {
				setShaderParameter(bars.hdrBar, param, value);
				setShaderParameter(bars.sdrBar, param, value);
			}
		}
	}

	private void setShaderParameter(org.godot.Godot bar, String parameter, Object value) {
		if (bar != null && bar.getProperty("material_override") instanceof ShaderMaterial mat) {
			mat.setShaderParameter(parameter, value);
		}
	}
}
